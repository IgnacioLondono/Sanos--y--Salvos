# geo-intelligence-service

Servicio de geointeligencia para zonas, riesgo y analitica geoespacial de reportes.

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
docker compose up -d geo-intelligence-service
docker compose logs -f geo-intelligence-service
```

Puerto: `8094`  
Health: `/api/zones/health`
