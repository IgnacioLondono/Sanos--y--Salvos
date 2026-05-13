# Sanos y Salvos - Plataforma de mascotas perdidas

Plataforma distribuida para registro, reporte y coincidencia de mascotas perdidas y encontradas. Implementada como un ecosistema de microservicios REST siguiendo el informe de arquitectura del proyecto.

## Arquitectura

- **8 microservicios Spring Boot** (IAM, Catalogo de mascotas, Reportes, Geo-inteligencia, Media, Matching IA, Capacity, Auditoria) con JPA + MySQL.
- **API Gateway** con validacion de JWT, CORS y rate limiting basico.
- **BFF (Backend for Frontend)** para agregacion resiliente (dashboard, mapa, detalle de mascota).
- **Frontend web** (HTML + JS vanilla) con pantallas por rol y mapa Leaflet/OpenStreetMap.
- **Database per Service**: un unico contenedor MySQL expone 8 esquemas aislados (`db_iam`, `db_pets`, `db_reports`, `db_geo`, `db_media`, `db_matching`, `db_capacity`, `db_audit`).
- **Seguridad**: contrasenas con BCrypt y JWT firmado (HMAC-SHA, 32+ chars) compartido entre IAM y Gateway.
- **Data seeding** en cada microservicio: al primer arranque la web ya trae mascotas, reportes, zonas, multimedia, capacity y auditoria de ejemplo.
- **OpenAPI / Swagger**: cada servicio expone su spec; el **gateway** agrega los 9 contratos (8 microservicios + BFF) en una sola Swagger UI con selector.

### Diagrama logico

```
[Frontend] -> [Gateway :8080] -> [BFF :8081]
                              -> [IAM :8091]          -> (db_iam)
                              -> [Pet Catalog :8092]  -> (db_pets)
                              -> [Reports :8093]      -> (db_reports)
                              -> [Geo :8094]          -> (db_geo)
                              -> [Media :8095]        -> (db_media)
                              -> [Matching :8096]     -> (db_matching)
                              -> [Capacity :8097]     -> (db_capacity)
                              -> [Audit :8098]        -> (db_audit)
```

## Ejecutar con Docker

Requisitos: Docker Desktop con Docker Compose v2.

```bash
docker compose up --build
```

Servicios principales:
- Frontend: http://localhost:5173
- API Gateway: http://localhost:8080
- BFF: http://localhost:8081
- MySQL: localhost:3306 (usuario `sanos`, password `sanos_pwd`)

Los 8 esquemas se crean automaticamente a partir de `db/init.sql`. Hibernate (`ddl-auto: update`) crea y evoluciona las tablas segun las entidades JPA.

## Documentacion API (Swagger / OpenAPI)

**Vista unificada (recomendada):** abre el gateway y usa el desplegable superior para cambiar de API.

- Swagger UI unificado: `http://localhost:8080/swagger-ui/index.html`
- Cada opcion carga el OpenAPI 3 del microservicio correspondiente (rutas internas proxy: `/openapi/{servicio}/v3/api-docs`).
- En la descripcion de cada API figuran el **esquema MySQL** y las **tablas** del dominio (17 tablas repartidas en 8 bases).
- Esquema de seguridad **Bearer JWT** documentado en cada servicio; para probar endpoints protegidos: `POST /api/iam/login` y luego **Authorize** en Swagger con el token.
- En codigo: cada **controlador** usa `@Tag`, `@Operation`, `@Parameter` y `@ApiResponse` donde aplica; **DTOs (records)** y **entidades JPA** llevan `@Schema` con descripcion de campos y mapeo a tablas SQL.

**Swagger directo por puerto** (sin gateway), por ejemplo: `http://localhost:8091/swagger-ui/index.html` (IAM), `8092` (mascotas), … `8098` (auditoria), `8081` (BFF).

## Credenciales pre-cargadas

- Administrador (login): `admin@sanosysalvos.cl` / `Admin#Sanos2026`
- Ciudadano de prueba: `ciudadano@sanosysalvos.cl` / `Ciudadano#2026`

## Pantallas del frontend

- `index.html` - Login unico (mismo formulario para ciudadano y admin; el rol del JWT define el dashboard).
- `register.html` - Registro ciudadano con RUT, comuna, contacto de emergencia, aceptacion de terminos.
- `admin-login.html` - Login exclusivo de administrador.
- `citizen-dashboard.html` - Mapa de reportes (clic para fijar coordenadas), alta de mascotas, reportes, multimedia, listados y coincidencias.
- `admin-dashboard.html` - Salud de microservicios, KPIs, usuarios IAM, reportes, capacity, mapa de zonas de riesgo (Leaflet), matching y auditoria.

Ambos dashboards integran Leaflet + OpenStreetMap sin requerir API key.

## Flujo de uso

1. Registrar un ciudadano: `POST /api/iam/register` (o desde `register.html`).
2. Autenticarse: `POST /api/iam/login` devuelve `{ token, id, email, displayName, role }`.
3. Enviar JWT en `Authorization: Bearer <token>` a todos los endpoints excepto los `/health` y login/register.
4. Consumir el dashboard agregado: `GET /api/bff/dashboard`.
5. Consumir datos de mapa: `GET /api/bff/map`.
6. Ejecutar motor de coincidencias: `POST /api/matching/run`.

## Endpoints principales

- IAM: `POST /api/iam/register`, `POST /api/iam/login`, `GET /api/iam/users`, `GET /api/iam/health`.
- Catalogo mascotas: `GET/POST /api/pets`, `GET /api/pets/{id}`, `DELETE /api/pets/{id}`, `GET /api/pets/by-chip/{chip}`, `GET /api/pets/owner/{ownerId}`.
- Reportes: `GET/POST /api/reports`, `GET /api/reports/{id}`, `GET /api/reports/pet/{petId}`, `GET /api/reports/status/{status}`, `PATCH /api/reports/{id}/status`.
- Geo: `GET/POST /api/zones`, `GET /api/zones/commune/{commune}`, `GET /api/zones/risk-summary`.
- Media: `GET/POST /api/media`, `GET /api/media/pet/{petId}`, `GET /api/media/report/{reportId}`.
- Matching IA: `GET /api/matching`, `POST /api/matching/run`, `GET /api/matching/report/{reportId}`.
- Capacity: `GET/POST /api/capacity`, `GET /api/capacity/zone/{zone}`, `GET /api/capacity/summary`.
- Auditoria: `GET /api/audit`, `GET /api/audit/entity/{entity}`, `GET /api/audit/actor/{actor}`.
- BFF: `GET /api/bff/dashboard`, `GET /api/bff/map`, `GET /api/bff/pet-overview/{petId}`, `GET /api/bff/health`.

Todos los servicios exponen `/api/<dominio>/health` como endpoint publico para el panel de estado del admin.

## Modelo de datos (17 tablas, 8 dominios en 3FN)

- **IAM**: `usuarios`, `credenciales`, `contactos_usuario`.
- **Catalogo**: `mascotas`, `caracteristicas_fisicas`, `vinculos_mascotas`.
- **Reportes**: `reportes_eventos`, `detalles_reporte`.
- **Geo**: `zonas_incidencia`, `coordenadas_reporte`.
- **Media**: `fotografias_mascotas`.
- **Matching**: `coincidencias_ia`, `desglose_similitud`.
- **Capacity**: `equipos_colaboracion`, `asignacion_capacidad`.
- **Auditoria**: `log_auditoria`, `notificaciones_sistema`.

## Desarrollo local (sin Docker)

1. Levantar MySQL 8 y aplicar `db/init.sql`.
2. Compilar y ejecutar cada microservicio con `mvn spring-boot:run` usando los puertos 8091-8098 y el BFF en 8081.
3. Ejecutar el gateway en 8080 con `SANOS_JWT_SECRET` alineado al del IAM.
4. Servir `frontend/` con cualquier servidor estatico (por ejemplo `npx http-server frontend -p 5173`).

## Troubleshooting

- Si el mapa no carga: verificar que el navegador puede acceder a `unpkg.com` (CDN de Leaflet) y `tile.openstreetmap.org`.
- Si `/api/bff/dashboard` responde con `serviceStatus` en DOWN para algun microservicio, el BFF seguira respondiendo el resto y el frontend mostrara los datos parciales sin romperse.
- Tokens: para regenerar credenciales cambia `SANOS_JWT_SECRET` en `docker-compose.yml` (sin olvidar replicarlo en IAM y Gateway).
