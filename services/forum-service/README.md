# forum-service

Servicio de foro comunitario (hilos y respuestas).

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
docker compose up -d forum-service
docker compose logs -f forum-service
```

Puerto: `8099`  
Health: `/api/forum/health` (segun configuracion de gateway/BFF)
