/** Navegación y redirecciones del dashboard. */

export const PAGE_REDIRECTS = {
  "admin-matching.html": "admin-operaciones.html",
  "admin-reportes.html": "admin-usuarios.html",
  "citizen-resumen.html": "citizen-reporte.html",
  "citizen-mascotas.html": "citizen-reporte.html",
  "citizen-fotos.html": "citizen-reporte.html"
};

export const ADMIN_PAGE_TITLES = {
  resumen: "Resumen operativo",
  usuarios: "Usuarios y reportes",
  reportes: "Usuarios y reportes",
  mapa: "Mapa y zonas",
  operaciones: "Coincidencias y operaciones",
  capacity: "Capacidad y refugios",
  matching: "Coincidencias y operaciones",
  auditoria: "Auditoría del sistema"
};

export const CITIZEN_PAGE_TITLES = {
  reporte: "Hacer reporte",
  mapa: "Mapa de la comunidad",
  mascotas: "Hacer reporte",
  fotos: "Hacer reporte",
  resumen: "Hacer reporte",
  actividad: "Actividad reciente",
  foro: "Foro de la comunidad",
  perfil: "Configuración de perfil"
};

export function resolveRedirect(currentFile) {
  const key = String(currentFile || "").toLowerCase();
  return PAGE_REDIRECTS[key] || null;
}

export function pageTitle(role, pageKey) {
  if (role === "admin") return ADMIN_PAGE_TITLES[pageKey] || "Panel administrador";
  if (role === "citizen") return CITIZEN_PAGE_TITLES[pageKey] || "Ciudadano";
  return "Sanos y Salvos";
}

export function detectRole(bodyClassList, dataset) {
  const classes = bodyClassList || [];
  const data = dataset || {};
  if (classes.includes("admin-page") || data.adminPage) return "admin";
  if (classes.includes("citizen-page") || data.citizenPage) return "citizen";
  return null;
}
