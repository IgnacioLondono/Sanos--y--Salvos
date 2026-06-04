# Comandos del proyecto (Docker, backend, frontend, pruebas)

> Ejecutar desde la raiz del repo: `Sanos--y--Salvos-main`

## Mapa (Google Maps)

Los mapas del ciudadano y del admin usan **Google Maps** (ya no OpenStreetMap/Leaflet).

1. Crea una API key en [Google Cloud Console](https://console.cloud.google.com/) y habilita **Maps JavaScript API**.
2. Pega la clave en `frontend/src/core/config.js`:

```javascript
googleMapsApiKey: "TU_API_KEY_AQUI",
```

3. Reconstruye el frontend si usas Docker:

```powershell
docker compose build frontend
docker compose up -d frontend
```

4. Recarga el navegador con **Ctrl+F5** en `http://localhost:5173`.

## Funciones recientes (mapa, registro, seguridad)

### Solicitudes de contacto en el mapa

1. Levanta el stack (ver sección Docker abajo).
2. Con dos usuarios ciudadanos, publica un reporte con el usuario A.
3. Con el usuario B: **Mapa** → clic en marcador de A → mensaje → **Enviar solicitud**.
4. Con el usuario A: panel **Solicitudes de contacto** → **Aceptar** o **Rechazar**.

API (vía gateway `http://localhost:8080`):

```http
POST /api/reports/contact-requests
GET  /api/reports/contact-requests/inbox?userId=1
PATCH /api/reports/contact-requests/{id}
```

Tras cambios en `reports-service`:

```powershell
docker compose build reports-service
docker compose up -d reports-service
```

### Registro: confirmar contraseña

Formulario en `http://localhost:5173/register.html`. No requiere rebuild de backend; solo frontend si cambias HTML/JS.

### Admin: acceso denegado sin cerrar sesión ciudadana

Un ciudadano logueado que abra `/pages/admin/admin-resumen.html` verá `acceso-denegado.html` y conservará su sesión. Para probar admin, usa login admin o cuenta con rol `ADMIN`.

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
