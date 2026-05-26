# capacity-service

Servicio para gestionar capacidad operativa, equipos y disponibilidad.

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
docker compose up -d capacity-service
docker compose logs -f capacity-service
```

Puerto: `8097`  
Salud: `/api/capacity/health`
