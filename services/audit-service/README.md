# audit-service

Servicio de auditoria de eventos y trazabilidad del sistema.

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
docker compose up -d audit-service
docker compose logs -f audit-service
```

Puerto: `8098`  
Salud: `/api/audit/health`
