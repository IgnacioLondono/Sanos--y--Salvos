# pet-catalog-service

Servicio de catalogo de mascotas y atributos asociados.

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
docker compose up -d pet-catalog-service
docker compose logs -f pet-catalog-service
```

Puerto: `8092`  
Salud: `/api/pets/health`
