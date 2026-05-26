# media-service

Servicio para carga y consulta de evidencias multimedia asociadas a mascotas y reportes.

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
docker compose up -d media-service
docker compose logs -f media-service
```

Puerto: `8095`  
Salud: `/api/media/health`
