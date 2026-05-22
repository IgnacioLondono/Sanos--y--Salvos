# Frontend — estructura

```
frontend/
├── index.html              # Login
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
| Login | `/index.html` |
| Foro | `/pages/citizen/citizen-foro.html` |
| Admin | `/pages/admin/admin-resumen.html` |

Con **Docker** (nginx), las URLs cortas siguen valiendo: `/citizen-foro.html` → redirige a `pages/citizen/`.

Con `npx http-server`, usa las rutas completas bajo `/pages/...`.

## Scripts en cada página

```html
<script src="../../src/core/paths.js"></script>
<script src="../../src/core/config.js"></script>
<script src="../../src/core/shared.js"></script>
<!-- ... -->
```
