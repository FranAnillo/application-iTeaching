#!/bin/bash
# Lanza backend (Spring Boot) y frontend (Vite) de iTeaching 2.0 en macOS/Linux

ROOT=$(pwd)
BACKEND="$ROOT/application"
FRONTEND="$ROOT/frontend"

echo ""
echo -e "\033[0;36m========================================\033[0m"
echo -e "\033[0;36m   iTeaching 2.0 - Inicio completo (macOS)\033[0m"
echo -e "\033[0;36m========================================\033[0m"
echo ""

# --- 1. Liberar puertos ---
echo -e "\033[0;33m[1/4] Liberando puertos 8081 y 5173...\033[0m"
lsof -ti:8081 | xargs kill -9 2>/dev/null
lsof -ti:5173 | xargs kill -9 2>/dev/null
sleep 2
echo -e "\033[0;32m       Puertos liberados.\033[0m"

# --- 2. Configurar JAVA_HOME --
export JAVA_HOME=$(/usr/libexec/java_home -v 17)
if [ -z "$JAVA_HOME" ]; then
    echo -e "\033[0;31m[ERROR] No se encontro Java 17. Por favor, instalalo.\033[0m"
    exit 1
fi
echo -e "\033[0;32m[2/4] JAVA_HOME = $JAVA_HOME\033[0m"

# --- 3. Arrancar Backend ---
echo -e "\033[0;33m[3/4] Arrancando backend (Spring Boot - perfil local, puerto 8081)...\033[0m"
cd "$BACKEND"
chmod +x mvnw
./mvnw spring-boot:run -Dspring-boot.run.profiles=local > backend.log 2>&1 &
BACKEND_PID=$!

# --- 4. Esperar a que el backend este listo ---
echo -e "\033[0;33m       Esperando a que el backend responda en http://localhost:8081...\033[0m"
READY=false
for i in {1..20}; do
    sleep 3
    if curl -s -o /dev/null -w "%{http_code}" http://localhost:8081/api/auth/login | grep -q "200\|401\|403\|405"; then
        READY=true
        break
    fi
    echo -e "\033[1;30m       ... esperando ($((i * 3))s)\033[0m"
done

if [ "$READY" = true ]; then
    echo -e "\033[0;32m       Backend listo!\033[0m"
else
    echo -e "\033[0;31m[ERROR] Backend no respondio tras varios intentos.\033[0m"
    kill $BACKEND_PID 2>/dev/null
    exit 1
fi

# --- 5. Arrancar Frontend ---
echo -e "\033[0;33m[4/4] Arrancando frontend (Vite dev server, puerto 5173)...\033[0m"
cd "$FRONTEND"
npm install > /dev/null 2>&1 # Instala dependencias por si acaso
npm run dev > frontend.log 2>&1 &
FRONTEND_PID=$!

sleep 5

echo ""
echo -e "\033[0;32m========================================\033[0m"
echo -e "\033[0;32m   iTeaching 2.0 - Todo arrancado!\033[0m"
echo -e "\033[0;32m========================================\033[0m"
echo ""
echo "  Frontend:    http://localhost:5173"
echo "  Backend API: http://localhost:8081/api"
echo "  H2 Console:  http://localhost:8081/h2-console"
echo ""
echo -e "\033[1;30m  Pulsa Ctrl+C para detener ambos servicios.\033[0m"
echo ""

# Mostrar los logs combinados
tail -f "$BACKEND/backend.log" "$FRONTEND/frontend.log" &
TAIL_PID=$!

# Capturar Ctrl+C para detener los procesos
trap 'echo -e "\n\033[0;33mDeteniendo servicios...\033[0m"; kill $BACKEND_PID $FRONTEND_PID $TAIL_PID 2>/dev/null; echo -e "\033[0;32mServicios detenidos. Hasta luego!\033[0m"; exit 0' SIGINT SIGTERM

wait
