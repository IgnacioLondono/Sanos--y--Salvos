# Frontend — estructura

```
frontend/
├── index.html              # Inicio de sesión
├── register.html           # Registro
├── pages/
│   ├── citizen/            # Panel ciudadano (*.html)
│   └── admin/              # Panel administrador (*.html)
├── assets/
│   ├── css/
│   └── images/
├── src/
│   ├── core/               # paths.js, config.js, shared.js
│   ├── ui/                 # theme-ui.js
│   ├── layout/             # dash-layout.js
│   ├── auth/               # login y registro
│   ├── citizen/            # dashboard y foro
│   ├── admin/              # dashboard admin
│   └── profile/            # user-profile.js
└── docs/
```

## URLs

| Qué | Ruta |
|-----|------|
| Inicio de sesión | `/index.html` |
| Foro | `/pages/citizen/citizen-foro.html` |
| Administrador | `/pages/admin/admin-resumen.html` |

Con **Docker** (nginx), las URLs cortas siguen valiendo: `/citizen-foro.html` → redirige a `pages/citizen/`.

Con `npx http-server`, usa las rutas completas bajo `/pages/...`.

## Pruebas unitarias (Vitest)

Cobertura mínima **60 %** sobre `src/lib/` (lógica de dominio del frontend).

```bash
cd frontend
npm install
npm test                 # ejecutar pruebas
npm run test:coverage    # reporte HTML en coverage/index.html
```

**Windows (PowerShell):** si aparece *«la ejecución de scripts está deshabilitada»*, usa `npm.cmd` en lugar de `npm`:

```powershell
cd frontend
npm.cmd install
npm.cmd test
npm.cmd run test:coverage
```

O, solo para la sesión actual: `Set-ExecutionPolicy -Scope Process -ExecutionPolicy Bypass`

Informe para la evaluación: [`docs/INFORME_PRUEBAS_FRONTEND.md`](../docs/INFORME_PRUEBAS_FRONTEND.md).

## Scripts en cada página

```html
<script src="../../src/core/paths.js"></script>
<script src="../../src/core/config.js"></script>
<script src="../../src/core/shared.js"></script>
<!-- ... -->
```
