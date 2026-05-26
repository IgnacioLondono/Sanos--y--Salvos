# Gateway

API Gateway del sistema. Centraliza seguridad JWT, enrutamiento y exposicion de endpoints para frontend y clientes.

## Requisitos

- Java 17
- Maven 3.9+

## Ejecutar en local

```bash
mvn spring-boot:run
```

## Probar

```bash
mvn test
```

## Docker

```bash
docker compose up -d gateway
docker compose logs -f gateway
```

Puerto por defecto: `8080`

## Endpoints útiles

- Salud: `/actuator/health`
- Swagger UI unificado: `/swagger-ui/index.html`
