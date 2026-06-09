/** Resolución de rutas relativas del frontend (sin DOM). */

export function inPages(pathname) {
  return String(pathname || "").includes("/pages/");
}

export function rootPrefix(pathname) {
  return inPages(pathname) ? "../../" : "./";
}

export function resolvePage(file, pathname) {
  if (!file) return rootPrefix(pathname);
  if (file === "index.html" || file === "register.html") {
    return rootPrefix(pathname) + file;
  }
  if (inPages(pathname)) {
    if (file.startsWith("admin-") && !pathname.includes("/pages/admin/")) {
      return rootPrefix(pathname) + "pages/admin/" + file;
    }
    if (file.startsWith("citizen-") && !pathname.includes("/pages/citizen/")) {
      return rootPrefix(pathname) + "pages/citizen/" + file;
    }
    return file;
  }
  if (file.startsWith("admin-")) return "./pages/admin/" + file;
  if (file.startsWith("citizen-")) return "./pages/citizen/" + file;
  return "./" + file;
}
