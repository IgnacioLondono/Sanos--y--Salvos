# BFF (Backend For Frontend)

Modulo de agregacion para vistas de frontend. Consume multiples microservicios y entrega respuestas consolidadas.

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
docker compose up -d bff
docker compose logs -f bff
```

Puerto por defecto: `8081`

## Endpoints utiles

- Health: `/api/bff/health`
- Dashboard: `/api/bff/dashboard`
