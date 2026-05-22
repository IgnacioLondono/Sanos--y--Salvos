# RabbitMQ — mensajería asíncrona (Sanos y Salvos)

## Objetivo académico

Cuando un ciudadano **crea un reporte**, el `reports-service` guarda el dato en MySQL y **publica un evento** en RabbitMQ. Otros microservicios reaccionan **sin bloquear** la respuesta HTTP:

| Rol | Servicio | Cola | Acción |
|-----|----------|------|--------|
| Productor | `reports-service` | — | Publica `report.created` en el exchange `sanos.events` |
| Consumidor | `audit-service` | `audit.report.created` | Escribe un registro en `log_auditoria` (`CREATE_ASYNC`) |
| Consumidor | `matching-service` | `matching.report.created` | Registra el evento y queda listo para el motor de coincidencias |

## Topología

```
reports-service  --(report.created)-->  [ Topic: sanos.events ]
                                              |
                    +-------------------------+-------------------------+
                    |                                                   |
            audit.report.created                          matching.report.created
                    |                                                   |
            audit-service                                     matching-service
```

- **Exchange:** `sanos.events` (topic, durable)
- **Routing key:** `report.created`
- **Payload JSON:** `ReportCreatedEvent` (`reportId`, `petId`, `createdByUserId`, `reportType`, `commune`, `status`, `occurredAt`)

## Docker

Con `docker compose up --build` se levanta **RabbitMQ Management**:

| Recurso | URL / puerto |
|---------|----------------|
| AMQP | `localhost:5672` |
| Consola web | http://localhost:15672 (usuario `sanos`, contraseña `sanos_pwd`) |

Los servicios `reports`, `audit` y `matching` reciben:

```env
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_USERNAME=sanos
SPRING_RABBITMQ_PASSWORD=sanos_pwd
```

## Desarrollo local (sin Docker)

1. Instala RabbitMQ o usa solo el contenedor: `docker compose up rabbitmq -d`
2. Variables por defecto en `application.yml`: host `localhost`, usuario `sanos`, password `sanos_pwd`
3. Arranca `reports-service`, `audit-service` y `matching-service` (Maven o IDE)

Para desactivar publicación si no hay broker: `SANOS_MESSAGING_ENABLED=false` en reports.

## Cómo demostrarlo al profesor

1. Abre http://localhost:15672 → **Queues** → deben existir `audit.report.created` y `matching.report.created` tras arrancar audit y matching.
2. Crea un reporte desde el frontend o `POST /api/reports` (con JWT).
3. En logs de `sanos-reports`: `Evento RabbitMQ publicado: report.created id=...`
4. En logs de `sanos-audit`: `RabbitMQ: reporte creado recibido id=...`
5. En la consola RabbitMQ, la cola `audit.report.created` muestra mensajes consumidos.
6. En admin → auditoría (o `GET /api/audit/logs`) aparece una fila con operación `CREATE_ASYNC` y `via":"RabbitMQ"` en el JSON.

## Código relevante

- `services/reports-service/.../messaging/ReportEventPublisher.java`
- `services/audit-service/.../messaging/ReportCreatedEventListener.java`
- `services/matching-service/.../messaging/ReportCreatedEventListener.java`
