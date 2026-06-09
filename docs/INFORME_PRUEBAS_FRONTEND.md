# Informe de pruebas unitarias — Frontend

**Proyecto:** Sanos y Salvos  
**Evaluación:** Parcial 3 — Desarrollo Fullstack III  
**Herramienta:** Vitest + @vitest/coverage-v8  
**Umbral mínimo (rúbrica):** 60 % de cobertura  
**Última ejecución:** 50 pruebas — Statements **90,2 %**, Lines **90,2 %**, Functions **97,7 %**, Branches **60 %**

## Alcance

Las pruebas cubren la **capa de dominio del frontend** (`frontend/src/lib/`), que concentra la lógica de negocio extraída de:

| Módulo lib | Componente UI relacionado |
|------------|---------------------------|
| `report-domain.js` | Historial de reportes, mapa, acciones encontrado/perdida |
| `validation.js` | Registro de ciudadanos (`register-auth.js`) |
| `contact-domain.js` | Panel contacto/chat del mapa (`map-contact.js`) |
| `api-utils.js` | Cliente HTTP (`shared.js`) |
| `datetime-utils.js` | Fechas Chile (`shared.js`) |
| `profile-domain.js` | Perfil ciudadano (`citizen-dashboard.js`) |
| `paths-logic.js` | Rutas relativas (`paths.js`) |
| `layout-domain.js` | Navegación dashboard (`dash-layout.js`) |
| `media-utils.js` | Fotos en mapa y reportes |
| `theme-domain.js` | Tema claro/oscuro (`theme-ui.js`) |
| `format-utils.js` | Sanitización HTML y textos |

## Cómo ejecutar

```bash
cd frontend
npm install
npm test
npm run test:coverage
```

En **PowerShell** con política de ejecución restrictiva: `npm.cmd install`, `npm.cmd test`, `npm.cmd run test:coverage`.

## Reportes generados

| Archivo | Descripción |
|---------|-------------|
| `frontend/coverage/index.html` | Reporte HTML interactivo (JaCoCo-style) |
| `frontend/coverage/coverage-summary.json` | Métricas JSON para el informe |
| Salida consola | Resumen `text-summary` al finalizar |

## Ejemplos de pruebas

### Reportes — marcar encontrado / volver a perdida

```javascript
reportQuickAction({ type: "LOST", status: "OPEN" })
// → { label: "Marcar encontrado", status: "RESOLVED" }

reportQuickAction({ type: "FOUND", status: "RESOLVED" })
// → { label: "Marcar perdida", status: "OPEN", type: "LOST" }
```

### Validación de registro

- RUT formato `12345678-9`
- Teléfono `+56 9 1234 5678`
- Contraseña fuerte (10+ chars, mayúscula, minúscula, número, símbolo)

### Contacto en mapa

- Badges de pestañas (recibidas, enviadas, chats, historial)
- Filtro solo pendientes en recibidas
- Ocultar «Cerrar chat» en historial

## Patrones de diseño

- **Módulos ES6** (`src/lib/`): separación de lógica de dominio y UI (IIFE en navegador).
- **Funciones puras**: facilitan pruebas unitarias sin DOM ni `fetch`.
- **Single responsibility**: cada archivo lib cubre un bounded context (reportes, contacto, IAM, etc.).

## Relación con backend

Los tests del frontend validan reglas que consumen la **API REST** del gateway (`:8080`): estados `LOST`/`FOUND`, códigos 401/429, fechas ISO UTC, etc., alineados con los microservicios Spring Boot.

## Para el entregable (ZIP / Blackboard)

La rúbrica pide un **PDF** con métricas y gráficos. Pasos sugeridos:

1. Abrir `frontend/coverage/index.html` en el navegador (se genera con `npm run test:coverage`).
2. Capturar pantalla del resumen y de 2–3 archivos con menor cobertura.
3. Exportar este documento a PDF (Word, VS Code, o imprimir a PDF).
4. Incluir en el ZIP: `frontend/tests/`, `frontend/src/lib/`, `frontend/package.json`, `frontend/vitest.config.js`, carpeta `frontend/coverage/` y este informe en PDF.

**Pendiente fuera del frontend:** diagrama de arquitectura (PNG/PDF), documento de persistencia (JPA), informe JaCoCo del backend (`mvn verify` → `target/site/jacoco/index.html`), y subir los cambios a GitHub.
