<#
.SYNOPSIS
    Lanza backend (Spring Boot) y frontend (Vite) de iTeaching 2.0
.DESCRIPTION
    - Libera puertos 8081 y 5173 si están ocupados
    - Arranca el backend con perfil local (H2, puerto 8081)
    - Arranca el frontend dev server (Vite, puerto 5173)
    - Ctrl+C para detener ambos
#>

$ROOT = Split-Path -Parent $MyInvocation.MyCommand.Path
$BACKEND = Join-Path $ROOT "application"
$FRONTEND = Join-Path $ROOT "frontend"

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "   iTeaching 2.0 - Inicio completo"     -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# --- 1. Liberar puertos ---
Write-Host "[1/4] Liberando puertos 8081 y 5173..." -ForegroundColor Yellow
Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue |
    ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue |
    ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
Start-Sleep -Seconds 2
Write-Host "       Puertos liberados." -ForegroundColor Green

# --- 2. Configurar JAVA_HOME ---
# proyecto compilado para Java 17; use JDK 17 si está instalado
$env:JAVA_HOME = "C:\Program Files\Java\jdk-17"
if (-not (Test-Path "$env:JAVA_HOME\bin\java.exe")) {
    # intentar detectar automáticamente un JDK 17 en el PATH
    $cmd = Get-Command java -ErrorAction SilentlyContinue
    if ($cmd) {
        $found = $cmd.Source
        Write-Host "[WARNING] No se encontro JDK17 en $env:JAVA_HOME, usando java de PATH ($found)" -ForegroundColor Yellow
    } else {
        Write-Host "[ERROR] No se encontro Java 17. Ajusta JAVA_HOME en este script o instala JDK17." -ForegroundColor Red
        exit 1
    }
}
Write-Host "[2/4] JAVA_HOME = $env:JAVA_HOME" -ForegroundColor Green

# --- 3. Arrancar Backend ---
Write-Host "[3/4] Arrancando backend (Spring Boot - perfil local, puerto 8081)..." -ForegroundColor Yellow
$backendJob = Start-Job -ScriptBlock {
    param($backendDir, $javaHome)
    $env:JAVA_HOME = $javaHome
    Set-Location $backendDir
    & .\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=local" 2>&1
} -ArgumentList $BACKEND, $env:JAVA_HOME

# --- 4. Esperar a que el backend este listo ---
Write-Host "       Esperando a que el backend responda en http://localhost:8081..." -ForegroundColor Yellow
$ready = $false
for ($i = 0; $i -lt 60; $i++) {
    Start-Sleep -Seconds 3
    try {
        $r = Invoke-WebRequest -Uri "http://localhost:8081/api/auth/login" -Method POST `
            -ContentType "application/json" -Body '{"username":"test","password":"test"}' `
            -UseBasicParsing -ErrorAction Stop
        $ready = $true; break
    } catch {
        if ($_.Exception.Response) { $ready = $true; break }
    }
    Write-Host "       ... esperando ($([int](($i+1)*3))s)" -ForegroundColor DarkGray
}

if ($ready) {
    Write-Host "       Backend listo!" -ForegroundColor Green
} else {
    Write-Host "[ERROR] Backend no respondio tras 3 minutos." -ForegroundColor Red
    Write-Host "        Revisa los logs con: Receive-Job $($backendJob.Id)" -ForegroundColor Red
    exit 1
}

# --- 5. Arrancar Frontend ---
Write-Host "[4/4] Arrancando frontend (Vite dev server, puerto 5173)..." -ForegroundColor Yellow
# make sure dependencies are installed (quietly)
$installJob = Start-Job -ScriptBlock {
    param($frontendDir)
    Set-Location $frontendDir
    if (-not (Test-Path "node_modules\vite")) {
        Write-Host "    instalando dependencias npm..." -ForegroundColor DarkYellow
        npm install > $null 2>&1
    }
} -ArgumentList $FRONTEND
# wait for install to finish
Wait-Job $installJob | Out-Null

$frontendJob = Start-Job -ScriptBlock {
    param($frontendDir)
    Set-Location $frontendDir
    & node node_modules\vite\bin\vite.js 2>&1
} -ArgumentList $FRONTEND

Start-Sleep -Seconds 5

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "   iTeaching 2.0 - Todo arrancado!"     -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "  Frontend:    http://localhost:5173"     -ForegroundColor White
Write-Host "  Backend API: http://localhost:8081/api" -ForegroundColor White
Write-Host "  H2 Console:  http://localhost:8081/h2-console" -ForegroundColor White
Write-Host ""
Write-Host "  Pulsa Ctrl+C para detener ambos servicios." -ForegroundColor DarkGray
Write-Host ""

# --- Mantener vivo y mostrar logs ---
try {
    while ($true) {
        # Mostrar output del backend
        $backendOutput = Receive-Job -Job $backendJob -ErrorAction SilentlyContinue
        if ($backendOutput) {
            $backendOutput | ForEach-Object {
                Write-Host "[BACKEND] $_" -ForegroundColor DarkCyan
            }
        }

        # Mostrar output del frontend
        $frontendOutput = Receive-Job -Job $frontendJob -ErrorAction SilentlyContinue
        if ($frontendOutput) {
            $frontendOutput | ForEach-Object {
                Write-Host "[FRONTEND] $_" -ForegroundColor DarkMagenta
            }
        }

        # Verificar que los jobs siguen vivos
        if ($backendJob.State -eq "Failed") {
            Write-Host "[ERROR] El backend se ha detenido inesperadamente." -ForegroundColor Red
            Receive-Job -Job $backendJob | Write-Host -ForegroundColor Red
            break
        }
        if ($frontendJob.State -eq "Failed") {
            Write-Host "[ERROR] El frontend se ha detenido inesperadamente." -ForegroundColor Red
            Receive-Job -Job $frontendJob | Write-Host -ForegroundColor Red
            break
        }

        Start-Sleep -Seconds 2
    }
} finally {
    # --- Limpieza al salir ---
    Write-Host ""
    Write-Host "Deteniendo servicios..." -ForegroundColor Yellow
    Stop-Job -Job $backendJob -ErrorAction SilentlyContinue
    Stop-Job -Job $frontendJob -ErrorAction SilentlyContinue
    Remove-Job -Job $backendJob -Force -ErrorAction SilentlyContinue
    Remove-Job -Job $frontendJob -Force -ErrorAction SilentlyContinue

    # Matar procesos en los puertos por si acaso
    Get-NetTCPConnection -LocalPort 8081 -ErrorAction SilentlyContinue |
        ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }
    Get-NetTCPConnection -LocalPort 5173 -ErrorAction SilentlyContinue |
        ForEach-Object { Stop-Process -Id $_.OwningProcess -Force -ErrorAction SilentlyContinue }

    Write-Host "Servicios detenidos. Hasta luego!" -ForegroundColor Green
}
