@echo off
setlocal enabledelayedexpansion
cd /d "%~dp0"

echo ============================================
echo   SANOS Y SALVOS - ENCENDIENDO TODO
echo ============================================
echo.
echo [1/3] Verificando Docker...
docker --version >nul 2>&1
if errorlevel 1 (
  echo.
  echo [ERROR] Docker no está instalado o no está disponible.
  pause
  exit /b 1
)

echo.
echo [2/3] Construyendo y levantando contenedores...
echo   - MySQL (Puerto 3307)
echo   - IAM Service (Puerto 8091)
echo   - Pet Catalog Service (Puerto 8092)
echo   - Reports Service (Puerto 8093)
echo   - Geo Intelligence Service (Puerto 8094)
echo   - Media Service (Puerto 8095)
echo   - Matching Service (Puerto 8096)
echo   - Capacity Service (Puerto 8097)
echo   - Audit Service (Puerto 8098)
echo   - BFF (Puerto 8081)
echo   - Gateway (Puerto 8080)
echo   - Frontend (Puerto 5173)
echo.

docker compose up --build -d
if errorlevel 1 (
  echo.
  echo [ERROR] No se pudieron iniciar los servicios.
  pause
  exit /b 1
)

echo.
echo [3/3] Servicios levantados correctamente.
echo.
echo ============================================
echo   ENDPOINTS DISPONIBLES
echo ============================================
echo.
echo Frontend:        http://localhost:5173
echo Gateway API:     http://localhost:8080
echo Swagger UI:      http://localhost:8080/swagger-ui/index.html
echo.
echo Servicios Individuales:
echo   - IAM Service:              http://localhost:8091
echo   - Pet Catalog Service:      http://localhost:8092
echo   - Reports Service:          http://localhost:8093
echo   - Geo Intelligence Service: http://localhost:8094
echo   - Media Service:            http://localhost:8095
echo   - Matching Service:         http://localhost:8096
echo   - Capacity Service:         http://localhost:8097
echo   - Audit Service:            http://localhost:8098
echo   - BFF:                      http://localhost:8081
echo.
echo ============================================
echo   COMANDOS ÚTILES
echo ============================================
echo.
echo Ver logs en vivo:
echo   docker compose logs -f
echo.
echo Ver logs de un servicio específico:
echo   docker compose logs -f [nombre-servicio]
echo.
echo Apagar todo: doble clic en apagar-todo.bat
echo.
pause
