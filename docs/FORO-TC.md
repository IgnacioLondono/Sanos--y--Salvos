# Documentacion tecnica — Microservicio Foro

## Identificacion

| Campo | Valor |
|-------|--------|
| Servicio | `forum-service` |
| Puerto | `8099` |
| Base de datos | `db_foro` |
| Prefijo API | `/api/forum` |
| Gateway | `http://localhost:8080/api/forum/**` |

## Tablas

### `hilos_foro`

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| `id_hilo` | BIGINT PK AI | Identificador del hilo |
| `titulo` | VARCHAR(200) | Titulo visible en el listado |
| `categoria` | VARCHAR(32) | `AYUDA`, `CONSEJOS` o `GENERAL` |
| `id_usuario` | BIGINT | FK logica a `usuarios.id_usuario` (IAM) |
| `nombre_autor` | VARCHAR(120) | Nombre para mostrar en UI |
| `fecha_creacion` | DATETIME | Alta del hilo |
| `fecha_actualizacion` | DATETIME | Ultima respuesta |

### `mensajes_foro`

| Columna | Tipo | Descripcion |
|---------|------|-------------|
| `id_mensaje` | BIGINT PK AI | Identificador del mensaje |
| `id_hilo` | BIGINT FK | Referencia a `hilos_foro.id_hilo` |
| `contenido` | TEXT | Cuerpo del mensaje o respuesta |
| `id_usuario` | BIGINT | Autor (IAM) |
| `nombre_autor` | VARCHAR(120) | Nombre visible |
| `fecha_creacion` | DATETIME | Fecha de publicacion |

Script SQL de referencia: `db/schema-foro.sql`.

## API REST

| Metodo | Ruta | Auth (gateway) | Descripcion |
|--------|------|----------------|-------------|
| GET | `/api/forum/health` | Publico | Healthcheck |
| GET | `/api/forum/threads` | Publico (GET) | Lista hilos; query `?category=AYUDA` |
| GET | `/api/forum/threads/{id}` | Publico (GET) | Hilo + mensajes |
| POST | `/api/forum/threads` | JWT Bearer | Crear hilo + primer mensaje |
| POST | `/api/forum/threads/{id}/posts` | JWT Bearer | Responder en un hilo |

### Ejemplo crear hilo

```http
POST /api/forum/threads HTTP/1.1
Host: localhost:8080
Authorization: Bearer <token>
Content-Type: application/json

{
  "title": "Como subir una foto al reporte?",
  "content": "No encuentro la opcion de adjuntar imagen en el formulario.",
  "category": "AYUDA",
  "authorId": 1,
  "authorName": "Ana Perez"
}
```

### Ejemplo respuesta

```http
POST /api/forum/threads/1/posts HTTP/1.1
Authorization: Bearer <token>
Content-Type: application/json

{
  "content": "En el paso 3 del reporte puedes elegir archivo de imagen.",
  "authorId": 2,
  "authorName": "Pedro Soto"
}
```

## Swagger / OpenAPI

| Entorno | URL |
|---------|-----|
| Servicio directo | http://localhost:8099/swagger-ui.html |
| OpenAPI JSON | http://localhost:8099/v3/api-docs |
| Gateway (selector **Foro**) | http://localhost:8080/swagger-ui/index.html |

Anotaciones: `@Tag`, `@Operation`, `@Schema` en controlador y DTOs; esquema de seguridad `bearer-jwt`.

## Frontend

| Archivo | Rol |
|---------|-----|
| `frontend/citizen-foro.html` | Pagina del foro |
| `frontend/src/citizen-forum.js` | Logica listado, detalle, nuevo hilo, respuestas |
| Menu | Entrada **Foro** en barra lateral ciudadano |

## Seed de datos

Al primer arranque sin hilos, `DataSeeder` inserta 3 hilos de ejemplo (ayuda, consejos, general).

## Diagrama de flujo

```
[Ciudadano UI] --GET/POST--> [Gateway :8080] --JWT--> [forum-service :8099]
                                                      |
                                                      v
                                              [MySQL db_foro]
                                              hilos_foro / mensajes_foro
```
