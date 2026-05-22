# Corrige: Access denied for user 'sanos'@'%' to database 'db_foro'
# Uso: .\scripts\fix-docker-mysql.ps1
# Requiere contenedor sanos-mysql en ejecucion (docker compose up -d mysql)

$ErrorActionPreference = "Stop"
$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$Sql = Join-Path $Root "db\docker-grants.sql"

Write-Host "Esperando MySQL (sanos-mysql)..."
$ok = $false
for ($i = 1; $i -le 30; $i++) {
    docker exec sanos-mysql mysqladmin ping -h localhost -uroot -proot 2>$null | Out-Null
    if ($LASTEXITCODE -eq 0) { $ok = $true; break }
    Start-Sleep -Seconds 2
}
if (-not $ok) {
    Write-Error "MySQL no responde. Ejecuta primero: docker compose up -d mysql"
}

Write-Host "Aplicando permisos en db_foro y demas bases..."
Get-Content $Sql -Raw | docker exec -i sanos-mysql mysql -uroot -proot
if ($LASTEXITCODE -ne 0) { exit $LASTEXITCODE }

Write-Host "Reiniciando forum-service..."
Set-Location $Root
docker compose restart forum-service

Write-Host "Listo. Prueba: http://localhost:8099/actuator/health"
