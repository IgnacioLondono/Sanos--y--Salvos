# Sanos y Salvos

Plataforma web para **registrar, reportar y encontrar mascotas perdidas**. Arquitectura de **microservicios** con Spring Boot, API Gateway, BFF y frontend por roles (ciudadano y administrador).

---

## Tabla de contenidos

1. [Características](#características)
2. [Arquitectura](#arquitectura)
3. [Stack tecnológico](#stack-tecnológico)
4. [Estructura del repositorio](#estructura-del-repositorio)
5. [Requisitos](#requisitos)
6. [Inicio rápido con Docker](#inicio-rápido-con-docker)
7. [Desarrollo local con XAMPP](#desarrollo-local-con-xampp)
8. [Puertos y URLs](#puertos-y-urls)
9. [Credenciales de prueba](#credenciales-de-prueba)
10. [Frontend](#frontend)
11. [API y documentación Swagger](#api-y-documentación-swagger)
12. [Modelo de datos](#modelo-de-datos)
13. [Desarrollo e IDE](#desarrollo-e-ide)
14. [Solución de problemas](#solución-de-problemas)
15. [Documentación adicional](#documentación-adicional)

---

## Características

| Área | Detalle |
|------|---------|
| **Ciudadano** | Login, registro, reporte unificado con mapa (Leaflet), perfil, historial, subida de fotos, foro comunitario |
| **Administrador** | Panel con KPIs, salud de microservicios, usuarios IAM, reportes, capacity, mapa de zonas, matching IA, auditoría |
| **Backend** | 9 microservicios + BFF + Gateway; JWT; rate limiting; circuit breaker en gateway |
| **Datos** | Patrón *database per service* (`db_iam`, `db_pets`, … `db_foro`) |
| **API** | OpenAPI 3 en cada servicio; Swagger UI unificada en el gateway |
| **Despliegue** | Docker Compose completo o desarrollo con XAMPP + Maven |

---

## Arquitectura

```
                         ┌─────────────────┐
                         │  Frontend :5173 │
                         │  (nginx / static)│
                         └────────┬────────┘
                                  │ HTTP
                         ┌────────▼────────┐
                         │  Gateway :8080  │
                         │  JWT · CORS     │
                         └────────┬────────┘
              ┌───────────────────┼───────────────────┐
              │                   │                   │
     ┌────────▼────────┐   ┌───────▼───────┐   ┌──────▼──────┐
     │   BFF :8081     │   │  IAM :8091    │   │ Foro :8099  │
     │  (agregación)   │   │  db_iam       │   │  db_foro    │
     └────────┬────────┘   └───────────────┘   └─────────────┘
              │
    ┌─────────┼─────────┬─────────┬─────────┬─────────┐
    │         │         │         │         │         │
 Pets:8092  Reports   Geo:8094  Media    Matching  Capacity  Audit
 db_pets    :8093     db_geo    :8095    :8096     :8097     :8098
            db_reports          db_media db_matching db_capacity db_audit
```

**Flujo típico**

1. El usuario entra por `index.html` → `POST /api/iam/login` → JWT.
2. Las peticiones van al **gateway** (`http://localhost:8080`).
3. Lecturas públicas (GET en reportes, mascotas, zonas, media, foro) no exigen token; escritura sí.
4. El panel admin y dashboards usan el **BFF** (`/api/bff/*`) para datos agregados.

---

## Stack tecnológico

| Capa | Tecnología |
|------|------------|
| Microservicios | Java 17, Spring Boot 3.3, Spring Data JPA, Springdoc OpenAPI |
| Gateway | Spring Cloud Gateway, Resilience4j |
| BFF | Spring Web, RestClient |
| Base de datos | MySQL 8.4 |
| Frontend | HTML5, CSS, JavaScript (vanilla), Leaflet + OpenStreetMap |
| Contenedores | Docker, Docker Compose v2 |
| Build | Maven 3.9 (`pom.xml` agregador en la raíz) |

---

## Estructura del repositorio

```
Sanos-y-Salvos/
├── bff/                      # Backend for Frontend (:8081)
├── gateway/                  # API Gateway (:8080)
├── frontend/                 # SPA estática (HTML/JS/CSS)
├── services/
│   ├── iam-service/          # :8091 → db_iam
│   ├── pet-catalog-service/  # :8092 → db_pets
│   ├── reports-service/      # :8093 → db_reports
│   ├── geo-intelligence-service/  # :8094 → db_geo
│   ├── media-service/        # :8095 → db_media
│   ├── matching-service/     # :8096 → db_matching
│   ├── capacity-service/     # :8097 → db_capacity
│   ├── audit-service/        # :8098 → db_audit
│   └── forum-service/        # :8099 → db_foro
├── db/
│   ├── init.sql              # Crea todas las bases db_*
│   └── schema-foro.sql       # Tablas del foro (opcional)
├── docs/
│   ├── XAMPP.md              # Guía detallada XAMPP
│   └── FORO-TC.md            # APIs y tablas del foro
├── scripts/
│   └── run-xampp.ps1         # Arranque rápido por servicio
├── docker-compose.yml
├── pom.xml                   # Agregador Maven (importar en el IDE)
├── encender-todo.bat         # Docker: levantar todo (incluye foro :8099)
└── apagar-todo.bat           # Docker: apagar todo
```

---

## Requisitos

### Docker (recomendado para demo completa)

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) con Compose v2
- ~8 GB RAM libres para todos los contenedores

### Desarrollo local (XAMPP / Maven)

- **Java 17+** y **Maven 3.9+**
- **MySQL 8** (XAMPP en puerto **3306** o Docker MySQL en **3307**)
- Navegador moderno
- Opcional: Extension Pack for Java (VS Code / Cursor) para depurar con F5

---

## Inicio rápido con Docker

### 1. Clonar y entrar al proyecto

```bash
git clone https://github.com/IgnacioLondono/Sanos--y--Salvos.git
cd Sanos--y--Salvos
```

### 2. Levantar el stack

**Windows (doble clic):** `encender-todo.bat` levanta **todo el stack**, incluido **forum-service** en el puerto 8099, y espera a que el foro responda antes de mostrar las URLs.

**Línea de comandos:**

```bash
docker compose up --build -d
```

### 3. Comprobar servicios

| Recurso | URL |
|---------|-----|
| **Frontend** | http://localhost:5173 |
| **API Gateway** | http://localhost:8080 |
| **Swagger unificado** | http://localhost:8080/swagger-ui/index.html |
| **Health foro** | http://localhost:8080/api/forum/health |
| **MySQL (desde el host)** | `localhost:3307` — usuario `sanos` / `sanos_pwd` |

### 4. Apagar

```bash
docker compose down
```

Para **borrar volúmenes** (bases nuevas con `db_*` separadas):

```bash
docker compose down -v
docker compose up --build -d
```

### Detalles Docker

- **MySQL** ejecuta `db/init.sql` al crear el volumen por primera vez.
- Cada microservicio usa su base **`db_*`** (no una sola base compartida).
- **Media** persiste archivos en el volumen `media_uploads` (`/data/uploads` en el contenedor).
- **JWT**: mismo `SANOS_JWT_SECRET` en IAM y gateway (ver `docker-compose.yml`).
- No uses XAMPP en **3306** y Docker MySQL en **3307** apuntando al mismo esquema sin saber qué entorno activo es.

---

## Desarrollo local con XAMPP

Guía paso a paso: **[docs/XAMPP.md](docs/XAMPP.md)**

### Resumen

1. XAMPP → iniciar **MySQL** (puerto 3306).
2. phpMyAdmin → importar **`db/init.sql`**.
3. Opcional: **`db/schema-foro.sql`** en `db_foro`.
4. Arrancar servicios con Maven o el script:

```powershell
# Desde la raíz del repo
.\scripts\run-xampp.ps1 -Service gateway
.\scripts\run-xampp.ps1 -Service iam
.\scripts\run-xampp.ps1 -Service forum
# ... ver tabla de servicios en docs/XAMPP.md
```

5. Frontend:

```powershell
cd frontend
npx --yes http-server . -p 5173 -c-1
```

6. En el navegador, API base: `http://localhost:8080` (configurable en `frontend/src/config.js`).

**Variables típicas XAMPP** (root sin contraseña):

```powershell
$env:SPRING_DATASOURCE_USERNAME = "root"
$env:SPRING_DATASOURCE_PASSWORD = ""
$env:SANOS_JWT_SECRET = "sanos-y-salvos-super-secret-key-at-least-32-chars"
```

---

## Puertos y URLs

| Componente | Puerto (host) | Base de datos (Docker) |
|------------|---------------|-------------------------|
| Frontend | 5173 | — |
| API Gateway | 8080 | — |
| BFF | 8081 | — |
| IAM | 8091 | `db_iam` |
| Catálogo mascotas | 8092 | `db_pets` |
| Reportes | 8093 | `db_reports` |
| Geo-inteligencia | 8094 | `db_geo` |
| Media | 8095 | `db_media` |
| Matching IA | 8096 | `db_matching` |
| Capacity | 8097 | `db_capacity` |
| Auditoría | 8098 | `db_audit` |
| **Foro** | **8099** | **`db_foro`** |
| MySQL (Docker) | **3307** → 3306 interno | todas las `db_*` |

---

## Credenciales de prueba

| Rol | Email | Contraseña |
|-----|-------|------------|
| Administrador | `admin@sanosysalvos.cl` | `Admin#Sanos2026` |
| Ciudadano | `ciudadano@sanosysalvos.cl` | `Ciudadano#2026` |

Los datos de ejemplo (mascotas, reportes, zonas, hilos de foro, etc.) se cargan con **DataSeeder** al primer arranque de cada microservicio.

---

## Frontend

Interfaz con **barra lateral** (`dash-layout.js` + `dash-sidebar.css`) y vistas por rol.

### Ciudadano

| Página | Descripción |
|--------|-------------|
| `index.html` | Login único (redirige según rol del JWT) |
| `register.html` | Registro con RUT, comuna, contacto de emergencia |
| `citizen-reporte.html` | Reporte en un solo flujo + mapa integrado |
| `citizen-mapa.html` | Mapa de reportes y zonas |
| `citizen-mascotas.html` | Mascotas del usuario |
| `citizen-foro.html` | Foro: hilos, respuestas, nuevo hilo |
| `citizen-perfil.html` | Datos personales e historial de reportes |
| `citizen-actividad.html` | Actividad reciente |

### Administrador

| Página | Descripción |
|--------|-------------|
| `admin-login.html` | Acceso solo administrador |
| `admin-resumen.html` | KPIs, salud de servicios, resumen operativo |
| `admin-operaciones.html` | Operaciones del día |
| `admin-reportes.html` | Gestión de reportes |
| `admin-mapa.html` | Mapa administrativo (Leaflet) |
| `admin-matching.html` | Coincidencias IA |
| `admin-capacity.html` | Capacidad y equipos |
| `admin-auditoria.html` | Logs y notificaciones |
| `admin-usuarios.html` | Usuarios IAM |

Mapas: **Leaflet** + tiles **OpenStreetMap** (sin API key).

---

## API y documentación Swagger

### Gateway (recomendado)

- **Swagger UI:** http://localhost:8080/swagger-ui/index.html  
- Selector con IAM, mascotas, reportes, geo, media, matching, capacity, auditoría, **foro** y BFF.
- OpenAPI proxy: `/openapi/{servicio}/v3/api-docs`

### Autenticación en Swagger

1. `POST /api/iam/login` con credenciales de prueba.
2. Copiar el `token`.
3. **Authorize** → `Bearer <token>`.

### Swagger por servicio (directo)

| Servicio | URL |
|----------|-----|
| IAM | http://localhost:8091/swagger-ui.html |
| Mascotas | http://localhost:8092/swagger-ui.html |
| Reportes | http://localhost:8093/swagger-ui.html |
| Geo | http://localhost:8094/swagger-ui.html |
| Media | http://localhost:8095/swagger-ui.html |
| Matching | http://localhost:8096/swagger-ui.html |
| Capacity | http://localhost:8097/swagger-ui.html |
| Auditoría | http://localhost:8098/swagger-ui.html |
| **Foro** | http://localhost:8099/swagger-ui.html |
| BFF | http://localhost:8081/swagger-ui.html |

### Endpoints principales

<details>
<summary><strong>IAM</strong> — <code>/api/iam</code></summary>

- `POST /register`, `POST /login`
- `GET /users`, `GET /users/me`, `PATCH /profile`
- `GET /health` (público)

</details>

<details>
<summary><strong>Mascotas</strong> — <code>/api/pets</code></summary>

- `GET/POST /`, `GET /{id}`, `DELETE /{id}`
- `GET /by-chip/{chip}`, `GET /owner/{ownerId}`

</details>

<details>
<summary><strong>Reportes</strong> — <code>/api/reports</code></summary>

- `GET/POST /`, `GET /{id}`, `GET /user/{userId}`
- `PATCH /{id}/status`

</details>

<details>
<summary><strong>Geo</strong> — <code>/api/zones</code></summary>

- `GET/POST /`, `GET /commune/{commune}`, `GET /risk-summary`

</details>

<details>
<summary><strong>Media</strong> — <code>/api/media</code></summary>

- `POST /upload` (multipart), `GET /pet/{petId}`, `GET /report/{reportId}`

</details>

<details>
<summary><strong>Matching</strong> — <code>/api/matching</code></summary>

- `GET /`, `POST /run`, `GET /report/{reportId}`

</details>

<details>
<summary><strong>Foro</strong> — <code>/api/forum</code> (ver <a href="docs/FORO-TC.md">FORO-TC.md</a>)</summary>

- `GET /threads`, `GET /threads/{id}`, `POST /threads`
- `POST /threads/{id}/posts`
- `GET /health` (público)

</details>

<details>
<summary><strong>BFF</strong> — <code>/api/bff</code></summary>

- `GET /dashboard`, `GET /map`, `GET /pet-overview/{petId}`

</details>

Todos los dominios exponen `GET /api/<dominio>/health` para el panel de estado del administrador.

---

## Modelo de datos

Patrón **una base por microservicio**. Hibernate (`ddl-auto: update`) crea y actualiza tablas al arrancar.

| Dominio | Base | Tablas principales |
|---------|------|-------------------|
| IAM | `db_iam` | `usuarios`, `credenciales`, `contactos_usuario` |
| Catálogo | `db_pets` | `mascotas`, `caracteristicas_fisicas`, `vinculos_mascotas` |
| Reportes | `db_reports` | `reportes_eventos`, `detalles_reporte` |
| Geo | `db_geo` | `zonas_incidencia`, `coordenadas_reporte` |
| Media | `db_media` | `fotografias_mascotas` |
| Matching | `db_matching` | `coincidencias_ia`, `desglose_similitud` |
| Capacity | `db_capacity` | `equipos_colaboracion`, `asignacion_capacidad` |
| Auditoría | `db_audit` | `log_auditoria`, `notificaciones_sistema` |
| Foro | `db_foro` | `hilos_foro`, `mensajes_foro` |

También existe `sanosysalvos` como base legacy opcional en `init.sql`.

---

## Desarrollo e IDE

### Maven (raíz)

```bash
mvn -pl services/forum-service -am package -DskipTests
```

### Depurar en VS Code / Cursor

1. Abrir la **raíz** del repo (donde está `pom.xml`).
2. **Java: Clean Java Language Server Workspace** → Reload.
3. **Run and Debug** → elegir por ejemplo **Geo — Run / Debug** o **Foro — Run / Debug**.

Configuración en `.vscode/launch.json` y tareas en `.vscode/tasks.json`.

### Compilar un solo servicio

```bash
cd services/iam-service
mvn spring-boot:run
```

---

## Solución de problemas

| Problema | Qué hacer |
|----------|-----------|
| Puerto ya en uso (ej. 8094) | Cerrar instancia anterior o usar la tarea `geo: liberar puerto 8094` en VS Code |
| `SpringApplication cannot be resolved` en el IDE | Abrir la raíz del repo, importar Maven, no usar Run sobre un `.java` suelto |
| Mapa en blanco | Comprobar acceso a `unpkg.com` (Leaflet) y `tile.openstreetmap.org` |
| BFF con servicios en DOWN | Arrancar el microservicio caído; el BFF devuelve datos parciales |
| JWT inválido | Mismo `SANOS_JWT_SECRET` en IAM y gateway |
| Docker: tablas vacías tras cambio a `db_*` | `docker compose down -v` y volver a subir |
| XAMPP + Docker a la vez | MySQL XAMPP en 3306, Docker en 3307; no mezclar URLs sin querer |
| Foro sin tablas | Importar `db/schema-foro.sql` o arrancar `forum-service` (Hibernate crea tablas) |
| Logs de Spring Boot en Docker Desktop | Normal al iniciar `sanos-forum`; no ejecutes el foro otra vez desde el IDE si Docker ya usa el 8099 |
| Puerto 8099 ocupado | `docker compose stop forum-service` o apaga todo con `apagar-todo.bat` antes de `mvn spring-boot:run` local |
| `Access denied for user 'sanos'@'%' to database 'db_foro'` | Volumen MySQL antiguo sin permisos. Ejecuta `.\scripts\fix-docker-mysql.ps1` o vuelve a correr `encender-todo.bat` (aplica `db/docker-grants.sql`) |

**Logs Docker:**

```bash
docker compose logs -f
docker compose logs -f forum-service
```

---

## Documentación adicional

| Documento | Contenido |
|-----------|-----------|
| [docs/XAMPP.md](docs/XAMPP.md) | MySQL local, scripts, puertos, frontend |
| [docs/FORO-TC.md](docs/FORO-TC.md) | Contratos REST y esquema del foro |
| [.env.example](.env.example) | Variables de referencia para Docker |

---

## Licencia y autor

Proyecto académico / demostración de arquitectura de microservicios.  
Repositorio: [github.com/IgnacioLondono/Sanos--y--Salvos](https://github.com/IgnacioLondono/Sanos--y--Salvos)
