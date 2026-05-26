# Guia XAMPP — Sanos y Salvos

Esta guia permite ejecutar el proyecto con **XAMPP** (Apache + MySQL + phpMyAdmin) en Windows, sin depender solo de Docker.

## Requisitos

- [XAMPP](https://www.apachefriends.org/) con **MySQL** activo (puerto **3306**).
- **Java 17** y **Maven** en el PATH.
- Navegador moderno.

## 1. Base de datos en XAMPP

1. Abre el **Panel de control XAMPP** e inicia **MySQL**.
2. Entra a **phpMyAdmin**: http://localhost/phpmyadmin
3. Ejecuta el script del proyecto:
   - Importa `db/init.sql` (crea `db_iam`, `db_pets`, … **`db_foro`**, etc.).
   - Opcional: importa `db/schema-foro.sql` para crear tablas del foro en **`db_foro`**.

**Credenciales tipicas XAMPP:**

| Parametro | Valor |
|-----------|--------|
| Host | `localhost` |
| Puerto | `3306` |
| Usuario | `root` |
| Contrasena | *(vacia)* |

Los microservicios usan por defecto `root` sin password en `localhost:3306`. Si tu XAMPP tiene otra clave, exporta variables antes de arrancar:

```powershell
$env:SPRING_DATASOURCE_URL="jdbc:mysql://localhost:3306/sanosysalvos?createDatabaseIfNotExist=true&serverTimezone=UTC&allowPublicKeyRetrieval=true&useSSL=false"
$env:SPRING_DATASOURCE_USERNAME="root"
$env:SPRING_DATASOURCE_PASSWORD="tu_password"
$env:SANOS_JWT_SECRET="sanos-y-salvos-super-secret-key-at-least-32-chars"
```

## 2. Compilar microservicios

Desde la raiz del repositorio:

```powershell
cd services\forum-service
mvn -DskipTests package
cd ..\..
```

Repite `mvn package` en cada servicio que vayas a usar, o compila todos desde tu IDE.

## 3. Arrancar servicios (puertos)

Abre **una terminal por servicio** (o usa el script `scripts/run-xampp.ps1`):

| Servicio | Puerto | Comando (ejemplo) |
|----------|--------|-------------------|
| IAM | 8091 | `mvn spring-boot:run` en `services/iam-service` |
| Catálogo de mascotas | 8092 | `services/pet-catalog-service` |
| Reportes | 8093 | `services/reports-service` |
| Geo (zonas) | 8094 | `services/geo-intelligence-service` |
| Media | 8095 | `services/media-service` |
| Coincidencias | 8096 | `services/matching-service` |
| Capacidad | 8097 | `services/capacity-service` |
| Auditoría | 8098 | `services/audit-service` |
| **Foro** | **8099** | `services/forum-service` |
| BFF | 8081 | `bff` |
| Gateway | 8080 | `gateway` |

**Foro con perfil XAMPP explicito:**

```powershell
cd services\forum-service
mvn spring-boot:run "-Dspring-boot.run.profiles=xampp"
```

## 4. Frontend

### Opcion A — Apache de XAMPP

1. Copia o enlaza la carpeta `frontend` dentro de `C:\xampp\htdocs\sanos\`.
2. Abre: http://localhost/sanos/index.html

En `frontend/src/config.js` (o ajustes guardados en localStorage) define la API:

```javascript
apiBaseUrl: "http://localhost:8080"
```

### Opcion B — Servidor estatico (recomendado para desarrollo)

```powershell
cd frontend
npx http-server . -p 5173 -c-1
```

Abre: http://localhost:5173

## 5. Verificar foro

1. Gateway activo: http://localhost:8080/api/forum/health → `{"status":"UP","service":"forum-service"}`
2. Swagger foro directo: http://localhost:8099/swagger-ui.html
3. Swagger unificado: http://localhost:8080/swagger-ui/index.html → selector **Foro**
4. Inicio de sesión ciudadano en el frontend → menú **Foro**

## 6. Tablas en phpMyAdmin

Tras arrancar `forum-service`, en la base **`db_foro`** debes ver:

- `hilos_foro`
- `mensajes_foro`

Si no aparecen, importa `db/schema-foro.sql` o revisa logs del servicio (Hibernate `ddl-auto: update`).

## 7. Docker y XAMPP a la vez

No uses el puerto **3306** dos veces. Si Docker MySQL esta en 3307 (`docker-compose.yml`), XAMPP puede quedarse en 3306 sin conflicto. Apunta cada entorno con `SPRING_DATASOURCE_URL` al puerto correcto.

## Credenciales de prueba

- Admin: `admin@sanosysalvos.cl` / `Admin#Sanos2026`
- Ciudadano: `ciudadano@sanosysalvos.cl` / `Ciudadano#2026`

## Documentacion relacionada

- README principal del repositorio
- `docs/FORO-TC.md` — APIs y tablas del foro
- Swagger UI en gateway y en cada puerto 8091–8099
