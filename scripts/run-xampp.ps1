# Arranque rapido para desarrollo con XAMPP (MySQL en localhost:3306, root sin password)
# Uso: desde la raiz del repo:  .\scripts\run-xampp.ps1 -Service forum
# Servicios: iam, pets, reports, geo, media, matching, capacity, audit, forum, bff, gateway, frontend

param(
    [ValidateSet("iam", "pets", "reports", "geo", "media", "matching", "capacity", "audit", "forum", "bff", "gateway", "frontend")]
    [string]$Service = "forum"
)

$Root = Split-Path -Parent (Split-Path -Parent $MyInvocation.MyCommand.Path)
$env:SANOS_JWT_SECRET = "sanos-y-salvos-super-secret-key-at-least-32-chars"
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = ""

$dbUrls = @{
    iam      = "jdbc:mysql://localhost:3306/db_iam?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    pets     = "jdbc:mysql://localhost:3306/db_pets?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    reports  = "jdbc:mysql://localhost:3306/db_reports?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    geo      = "jdbc:mysql://localhost:3306/db_geo?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    media    = "jdbc:mysql://localhost:3306/db_media?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    matching = "jdbc:mysql://localhost:3306/db_matching?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    capacity = "jdbc:mysql://localhost:3306/db_capacity?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    audit    = "jdbc:mysql://localhost:3306/db_audit?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
    forum    = "jdbc:mysql://localhost:3306/db_foro?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
}

$map = @{
    iam      = @{ Path = "services\iam-service"; Port = 8091; Extra = "" }
    pets     = @{ Path = "services\pet-catalog-service"; Port = 8092; Extra = "" }
    reports  = @{ Path = "services\reports-service"; Port = 8093; Extra = "" }
    geo      = @{ Path = "services\geo-intelligence-service"; Port = 8094; Extra = "" }
    media    = @{ Path = "services\media-service"; Port = 8095; Extra = "" }
    matching = @{ Path = "services\matching-service"; Port = 8096; Extra = "" }
    capacity = @{ Path = "services\capacity-service"; Port = 8097; Extra = "" }
    audit    = @{ Path = "services\audit-service"; Port = 8098; Extra = "" }
    forum    = @{ Path = "services\forum-service"; Port = 8099; Extra = "-Dspring-boot.run.profiles=xampp" }
    bff      = @{ Path = "bff"; Port = 8081; Extra = "" }
    gateway  = @{ Path = "gateway"; Port = 8080; Extra = "" }
}

if ($Service -eq "frontend") {
    Set-Location (Join-Path $Root "frontend")
    Write-Host "Frontend en http://localhost:5173 (API: http://localhost:8080)"
    npx --yes http-server . -p 5173 -c-1
    exit
}

$cfg = $map[$Service]
$dir = Join-Path $Root $cfg.Path
if (-not (Test-Path $dir)) {
    Write-Error "No existe: $dir"
    exit 1
}

Set-Location $dir
if ($dbUrls.ContainsKey($Service)) {
    $env:SPRING_DATASOURCE_URL = $dbUrls[$Service]
}
Write-Host "Iniciando $Service en puerto $($cfg.Port) con MySQL XAMPP (root@localhost:3306)..."
if ($dbUrls.ContainsKey($Service)) {
    Write-Host "Base de datos: $($dbUrls[$Service] -replace '.*://[^/]+/','' -replace '\?.*','')"
}
if ($cfg.Extra) {
    mvn spring-boot:run $cfg.Extra
} else {
    mvn spring-boot:run
}
