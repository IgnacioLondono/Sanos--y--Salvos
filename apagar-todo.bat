@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================
echo   SANOS Y SALVOS - APAGANDO TODO
echo ============================================
echo.
echo [1/2] Deteniendo contenedores...
echo   - Frontend
echo   - Gateway
echo   - BFF
echo   - 9 Microservicios
echo   - MySQL
echo.

docker compose down
if errorlevel 1 (
  echo.
  echo [ERROR] Hubo un problema al detener los servicios.
  pause
  exit /b 1
)

echo.
echo [2/2] Servicios detenidos correctamente.
echo.
echo ============================================
echo   ESTADO
echo ============================================
echo.
echo Todos los contenedores han sido parados y eliminados.
echo.
echo Para limpiar completamente (incluyendo volúmenes de datos):
echo   docker compose down -v
echo.
pause
