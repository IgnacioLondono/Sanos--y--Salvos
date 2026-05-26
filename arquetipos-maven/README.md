# Arquetipos Maven base (Parcial 2)

Esta carpeta contiene plantillas base usadas como referencia para crear nuevos componentes backend con estructura consistente.

## Contenido

- `microservice-base/`: plantilla de microservicio Spring Boot.
- `bff-base/`: plantilla de Backend For Frontend Spring Boot.

## Uso sugerido

1. copiar la carpeta base requerida.
2. reemplazar `artifactId`, `name`, `package` y puertos.
3. agregar controladores/servicios/repositorios del dominio.

## Estructura esperada para nuevos modulos

- `src/main/java/<paquete>/controller`
- `src/main/java/<paquete>/service`
- `src/main/java/<paquete>/repository`
- `src/main/resources/application.yml`
- `src/test/java/...`

## Nota

Estas plantillas se alinean con la estructura aplicada en `services/*`, `bff` y `gateway` del proyecto principal.
