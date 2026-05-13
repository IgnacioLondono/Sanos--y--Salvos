@echo off
setlocal
cd /d "%~dp0"

echo ============================================
echo   SANOS Y SALVOS - APAGANDO TODO
echo ============================================
echo.
docker compose down
if errorlevel 1 (
  echo.
  echo [ERROR] Hubo un problema al detener los servicios.
  pause
  exit /b 1
)

echo.
echo Servicios detenidos correctamente.
echo.
pause
