# Comandos del proyecto (Docker, backend, frontend, pruebas)

> Ejecutar desde la raiz del repo: `Sanos--y--Salvos-main`

## 1) Docker - ciclo principal

### Levantar todo (build + up)

```powershell
docker compose up --build -d
```

### Levantar todo (si ya construiste imagenes)

```powershell
docker compose up -d
```

### Rebuild completo y recrear contenedores

```powershell
docker compose build --parallel
docker compose up -d --force-recreate
```

### Ver estado de servicios

```powershell
docker compose ps
```

### Ver logs generales

```powershell
docker compose logs -f
```

### Ver logs por servicio

```powershell
docker compose logs -f gateway
docker compose logs -f bff
docker compose logs -f reports-service
docker compose logs -f iam-service
```

### Reiniciar un servicio puntual

```powershell
docker compose restart gateway
docker compose restart frontend
```

### Apagar stack

```powershell
docker compose down
```

### Apagar y borrar volumenes (reset total)

```powershell
docker compose down -v
```

---

## 2) Endpoints utiles

```text
Frontend:         http://localhost:5173
Gateway:          http://localhost:8080
BFF:              http://localhost:8081
Swagger Gateway:  http://localhost:8080/swagger-ui/index.html
RabbitMQ UI:      http://localhost:15672
MySQL host:       localhost:3307
```

---

## 3) Frontend (modo NPM local)

```powershell
cd frontend
npm install
npm run start
```

---

## 4) Maven - pruebas

### Todas las pruebas del proyecto

```powershell
mvn test -DskipITs
```

### Pruebas por modulo (ejemplos)

```powershell
mvn -pl gateway test
mvn -pl bff test
mvn -pl services/iam-service test
mvn -pl services/reports-service test
```

---

## 5) Cobertura JaCoCo

### Generar cobertura por modulo

```powershell
mvn -pl gateway verify -DskipITs
mvn -pl bff verify -DskipITs
mvn -pl services/reports-service verify -DskipITs
```

### Abrir reportes HTML

```text
gateway/target/site/jacoco/index.html
bff/target/site/jacoco/index.html
services/<modulo>/target/site/jacoco/index.html
```

---

## 6) Salud rapida de Gateway

```powershell
Invoke-WebRequest -Uri "http://localhost:8080/actuator/health" -UseBasicParsing
```

---

## 7) Build optimizado (evitar saturar CPU)

```powershell
$env:COMPOSE_PARALLEL_LIMIT=4
docker compose build --parallel
```

---

## 8) Comandos Git basicos

```powershell
git status
git add .
git commit -m "mensaje"
git push
```

---

## 9) Flujo recomendado rapido (daily)

```powershell
docker compose up -d
docker compose ps
docker compose logs -f gateway
```
