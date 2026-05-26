# reports-service

Servicio de reportes de mascotas (perdidas/encontradas) y publicacion de eventos a RabbitMQ.

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
Health: `/api/reports/health`
