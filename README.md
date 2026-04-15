# Sanos y Salvos - Microservicios REST

Arquitectura implementada:
- 8 microservicios Spring Boot (IAM, Catálogo, Reportes, Geo, Media, Matching, Capacity, Auditoría)
- API Gateway con validación JWT, CORS y rate limiting básico
- BFF (Backend for Frontend) para agregación de datos
- Frontend minimalista conectado al Gateway
- Docker Compose para levantar todo

## Ejecutar con Docker

`ash
docker compose up --build
`

Servicios principales:
- Frontend: http://localhost:5173
- Gateway: http://localhost:8080
- BFF: http://localhost:8081

## Flujo rápido

1. Registrar usuario:
   - POST /api/iam/register
2. Login:
   - POST /api/iam/login -> retorna JWT
3. Usar token en Authorization: Bearer <token> para el resto de endpoints
4. Consumir dashboard agregado:
   - GET /api/bff/dashboard

## Nota técnica

Esta implementación es un MVP funcional y usa almacenamiento en memoria para acelerar validación y demo arquitectónica.
