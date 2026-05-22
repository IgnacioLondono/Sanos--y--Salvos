@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================
echo   SANOS Y SALVOS - ENCENDIENDO TODO
echo ============================================
echo.

echo [1/5] Verificando Docker...
docker --version >nul 2>&1
if errorlevel 1 (
  echo [ERROR] Docker no esta instalado o no esta disponible.
  pause
  exit /b 1
)

docker ps --filter "name=sanos-gateway" --format "{{.Names}}" 2>nul | findstr /i "sanos-gateway" >nul
set STACK_YA_CORRIENDO=!errorlevel!

echo.
if !STACK_YA_CORRIENDO! equ 0 (
  echo [2/5] Stack ya activo en Docker Desktop. Sincronizando servicios...
  docker compose up -d
) else (
  echo [2/5] Construyendo y levantando contenedores...
  echo   MySQL 3307 ^| servicios 8091-8099 ^| BFF 8081 ^| Gateway 8080 ^| Frontend 5173
  docker compose up --build -d
)

if errorlevel 1 (
  echo [ERROR] No se pudieron iniciar los servicios.
  pause
  exit /b 1
)

echo.
echo [3/5] Permisos MySQL ^(db_foro para usuario sanos^)...
set MYSQL_OK=0
for /L %%i in (1,1,25) do (
  docker exec sanos-mysql mysqladmin ping -h localhost -uroot -proot >nul 2>&1
  if !errorlevel! equ 0 (
    set MYSQL_OK=1
    goto :mysql_up
  )
  timeout /t 2 /nobreak >nul
)
:mysql_up
if !MYSQL_OK! equ 0 (
  echo   [AVISO] MySQL no responde aun. Si el foro falla, ejecuta: scripts\fix-docker-mysql.ps1
  goto :skip_grants
)
docker exec -i sanos-mysql mysql -uroot -proot < "%~dp0db\docker-grants.sql" >nul 2>&1
if errorlevel 1 (
  echo   [AVISO] No se pudieron aplicar grants. Ejecuta: powershell -File scripts\fix-docker-mysql.ps1
) else (
  echo   Permisos OK ^(db_foro, db_iam, ...^)
  docker compose restart forum-service >nul 2>&1
)
:skip_grants

echo.
echo [4/5] Esperando foro (forum-service)...
set FORO_OK=0
for /L %%i in (1,1,40) do (
  powershell -NoProfile -Command "try { $r = Invoke-WebRequest -Uri 'http://localhost:8099/actuator/health' -UseBasicParsing -TimeoutSec 4; if ($r.StatusCode -eq 200) { exit 0 } else { exit 1 } } catch { exit 1 }" >nul 2>&1
  if !errorlevel! equ 0 (
    set FORO_OK=1
    goto :foro_ready
  )
  timeout /t 3 /nobreak >nul
)
:foro_ready
if !FORO_OK! equ 0 (
  echo   [AVISO] Foro no responde. Error tipico: Access denied db_foro
  echo   Solucion: powershell -ExecutionPolicy Bypass -File scripts\fix-docker-mysql.ps1
  echo   Logs: docker compose logs -f forum-service
) else (
  echo   Foro OK
)

echo.
echo [5/5] Listo.
echo.
echo Frontend:  http://localhost:5173/citizen-foro.html
echo API:       http://localhost:8080/api/forum/threads
echo Health:    http://localhost:8080/api/forum/health
echo.
echo No ejecutes el foro desde el IDE si Docker usa el puerto 8099.
echo Apagar: apagar-todo.bat
echo.
pause
