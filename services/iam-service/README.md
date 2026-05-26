# iam-service

Servicio de identidad y acceso (autenticacion, perfil, usuarios, JWT).

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
docker compose up -d iam-service
docker compose logs -f iam-service
```

Puerto: `8091`  
Health: `/api/iam/health`
