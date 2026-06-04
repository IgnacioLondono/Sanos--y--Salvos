# reports-service

Servicio de reportes de mascotas (perdidas/encontradas), **solicitudes de contacto en el mapa** y publicación de eventos a RabbitMQ.

## Solicitudes de contacto (mapa)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `POST` | `/api/reports/contact-requests` | Enviar solicitud (`reportId`, `fromUserId`, `message`) |
| `GET` | `/api/reports/contact-requests/inbox?userId=` | Recibidas (receptor = dueño del reporte) |
| `GET` | `/api/reports/contact-requests/sent?userId=` | Enviadas |
| `PATCH` | `/api/reports/contact-requests/{id}` | Responder: `ACCEPTED` o `REJECTED` |

Estados: `PENDING`, `ACCEPTED`, `REJECTED`. Tabla: `solicitudes_contacto` en `db_reports`.

Al aceptar (`ACCEPTED`) se crea una conversación de chat (`OPEN`). Tablas: `conversaciones_contacto`, `mensajes_contacto`.

## Conversaciones de contacto (chat)

| Método | Ruta | Descripción |
|--------|------|-------------|
| `GET` | `/api/reports/contact-conversations?userId=&status=` | Listar (`OPEN`, `CLOSED`, `ALL`) |
| `GET` | `/api/reports/contact-conversations/{id}/messages?userId=` | Mensajes del chat |
| `POST` | `/api/reports/contact-conversations/{id}/messages` | Enviar mensaje (`authorUserId`, `content`) |
| `PATCH` | `/api/reports/contact-conversations/{id}/close` | Cerrar chat (`userId`; solo receptor) |

Estados de conversación: `OPEN`, `CLOSED`.

Swagger: `http://localhost:8093/swagger-ui/index.html` (tags **Solicitudes de contacto** y **Conversaciones de contacto**).

## Ejecutar

```bash
mvn spring-boot:run
```

## Pruebas

```bash
mvn test
```

## Docker

```bash
docker compose up -d reports-service
docker compose logs -f reports-service
```

Puerto: `8093`  
Salud: `/api/reports/health`
