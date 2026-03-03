$base = 'http://localhost:8081/api'
$results = @()

# Login all 3
$r = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"admin","password":"Admin1234"}'
$adminToken = $r.token
$r2 = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"profesor","password":"Profe123!"}'
$profToken = $r2.token
$r3 = Invoke-RestMethod -Uri "$base/auth/login" -Method POST -ContentType 'application/json' -Body '{"username":"estudiante","password":"Estud123!"}'
$estToken = $r3.token
$results += "1. Logins OK: Admin=$($r.role) Prof=$($r2.role) Est=$($r3.role)"

$adminH = @{Authorization="Bearer $adminToken"; 'Content-Type'='application/json'}
$profH = @{Authorization="Bearer $profToken"; 'Content-Type'='application/json'}
$estH = @{Authorization="Bearer $estToken"; 'Content-Type'='application/json'}

# 2. Admin creates asignatura
$asig = Invoke-RestMethod -Uri "$base/asignaturas" -Method POST -Headers $adminH -Body '{"nombre":"Matematicas I","descripcion":"Curso de mate","url":"mat1"}'
$asigId = $asig.id
$results += "2. Admin create asig: OK (id=$asigId)"

# 3. Profesor tries to create asignatura (should fail 403)
try {
    Invoke-RestMethod -Uri "$base/asignaturas" -Method POST -Headers $profH -Body '{"nombre":"Test","descripcion":"x","url":"x"}'
    $results += "3. Prof create asig: FAIL (should be denied)"
} catch {
    $status = [int]$_.Exception.Response.StatusCode
    $results += "3. Prof create asig: DENIED ($status) - OK"
}

# 4. Estudiante tries to create asignatura (should fail 403)
try {
    Invoke-RestMethod -Uri "$base/asignaturas" -Method POST -Headers $estH -Body '{"nombre":"Test2","descripcion":"x","url":"x2"}'
    $results += "4. Est create asig: FAIL (should be denied)"
} catch {
    $status = [int]$_.Exception.Response.StatusCode
    $results += "4. Est create asig: DENIED ($status) - OK"
}

# 5. Admin assigns profesor to asignatura
# Get profesor persona id
$personas = Invoke-RestMethod -Uri "$base/usuarios" -Method GET -Headers $adminH
$profPersona = $personas | Where-Object { $_.username -eq 'profesor' } | Select-Object -First 1
$estPersona = $personas | Where-Object { $_.username -eq 'estudiante' } | Select-Object -First 1
if (-not $profPersona) { $profPersona = $personas | Where-Object { $_.nombre -eq 'Profesor' } | Select-Object -First 1 }
if (-not $estPersona) { $estPersona = $personas | Where-Object { $_.nombre -eq 'Estudiante' } | Select-Object -First 1 }
$results += "5a. Personas found: prof=$($profPersona.id) est=$($estPersona.id)"

try {
    Invoke-RestMethod -Uri "$base/asignaturas/$asigId/profesores/$($profPersona.id)" -Method POST -Headers $adminH
    $results += "5b. Admin add prof to asig: OK"
} catch {
    $results += "5b. Admin add prof to asig: FAIL - $($_.Exception.Message)"
}

# 6. Admin assigns estudiante to asignatura
try {
    Invoke-RestMethod -Uri "$base/asignaturas/$asigId/estudiantes/$($estPersona.id)" -Method POST -Headers $adminH
    $results += "6. Admin add est to asig: OK"
} catch {
    $results += "6. Admin add est to asig: FAIL - $($_.Exception.Message)"
}

# 7. Verify asignatura has both
$asigCheck = Invoke-RestMethod -Uri "$base/asignaturas/$asigId" -Method GET -Headers $adminH
$results += "7. Asig check: profs=$($asigCheck.profesorIds.Count) ests=$($asigCheck.estudianteIds.Count)"

# 8. Profesor creates grupo
try {
    $grupo = Invoke-RestMethod -Uri "$base/grupos" -Method POST -Headers $profH -Body "{`"nombre`":`"Grupo T1`",`"tipo`":`"TEORIA`",`"asignaturaId`":$asigId}"
    $grupoId = $grupo.id
    $results += "8. Prof create grupo TEORIA: OK (id=$grupoId)"
} catch {
    $results += "8. Prof create grupo: FAIL - $($_.Exception.Message)"
}

# 9. Profesor creates grupo PRACTICA
try {
    $grupo2 = Invoke-RestMethod -Uri "$base/grupos" -Method POST -Headers $profH -Body "{`"nombre`":`"Grupo P1`",`"tipo`":`"PRACTICA`",`"asignaturaId`":$asigId}"
    $results += "9. Prof create grupo PRACTICA: OK (id=$($grupo2.id))"
} catch {
    $results += "9. Prof create grupo PRACTICA: FAIL - $($_.Exception.Message)"
}

# 10. Profesor creates carpeta
try {
    $carpeta = Invoke-RestMethod -Uri "$base/carpetas" -Method POST -Headers $profH -Body "{`"nombre`":`"Tema 1`",`"asignaturaId`":$asigId}"
    $carpetaId = $carpeta.id
    $results += "10. Prof create carpeta: OK (id=$carpetaId)"
} catch {
    $results += "10. Prof create carpeta: FAIL - $($_.Exception.Message)"
}

# 11. Estudiante tries to create grupo (should fail 403)
try {
    Invoke-RestMethod -Uri "$base/grupos" -Method POST -Headers $estH -Body "{`"nombre`":`"Test`",`"tipo`":`"TEORIA`",`"asignaturaId`":$asigId}"
    $results += "11. Est create grupo: FAIL (should be denied)"
} catch {
    $status = [int]$_.Exception.Response.StatusCode
    $results += "11. Est create grupo: DENIED ($status) - OK"
}

# 12. Estudiante tries to create carpeta (should fail 403)
try {
    Invoke-RestMethod -Uri "$base/carpetas" -Method POST -Headers $estH -Body "{`"nombre`":`"Test`",`"asignaturaId`":$asigId}"
    $results += "12. Est create carpeta: FAIL (should be denied)"
} catch {
    $status = [int]$_.Exception.Response.StatusCode
    $results += "12. Est create carpeta: DENIED ($status) - OK"
}

# 13. List grupos for asignatura
try {
    $grupos = Invoke-RestMethod -Uri "$base/grupos/asignatura/$asigId" -Method GET -Headers $profH
    $results += "13. List grupos: $($grupos.Count) found"
} catch {
    $results += "13. List grupos: FAIL - $($_.Exception.Message)"
}

# 14. List carpetas for asignatura
try {
    $carpetas = Invoke-RestMethod -Uri "$base/carpetas/asignatura/$asigId" -Method GET -Headers $profH
    $results += "14. List carpetas: $($carpetas.Count) found"
} catch {
    $results += "14. List carpetas: FAIL - $($_.Exception.Message)"
}

# Output all results
$results | Out-File 'C:\Users\fjanillo\Documents\application-iTeaching\test_results.txt' -Encoding utf8
