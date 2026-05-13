@echo off
setlocal
cd /d "%~dp0"

echo ============================================
echo   SANOS Y SALVOS - ENCENDIENDO TODO
echo ============================================
echo.
echo [1/2] Construyendo y levantando contenedores...
docker compose up --build -d
if errorlevel 1 (
  echo.
  echo [ERROR] No se pudieron iniciar los servicios.
  pause
  exit /b 1
)

echo.
echo [2/2] Servicios levantados.
echo.
echo Frontend: http://localhost:5173
echo Gateway:  http://localhost:8080
echo Swagger unificado (9 APIs): http://localhost:8080/swagger-ui/index.html
echo.
echo Para ver logs en vivo: docker compose logs -f
echo Para apagar todo: doble clic en apagar-todo.bat
echo.
pause
