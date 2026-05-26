# matching-service

Servicio de matching para detectar coincidencias entre reportes de mascotas.

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
docker compose up -d matching-service
docker compose logs -f matching-service
```

Puerto: `8096`  
Salud: `/api/matching/health`
