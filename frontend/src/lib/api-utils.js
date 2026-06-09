/** Utilidades HTTP, sesión y URLs de API/media. */

export function sanitizeBaseUrl(value) {
  return String(value || "").trim().replace(/\/$/, "");
}

export function inferApiBaseUrl(protocol = "http:", hostname = "localhost") {
  return `${protocol}//${hostname}:8080`;
}

export function normalizeToken(token) {
  if (!token) return "";
  let value = String(token).trim();
  if (/^bearer\s+/i.test(value)) {
    value = value.replace(/^bearer\s+/i, "").trim();
  }
  return value;
}

export function apiErrorMessage(status, path, data) {
  const p = String(path || "");
  if (status === 401) {
    if (p.includes("/api/iam/login")) {
      return "Datos incorrectos, revise correo u contraseña";
    }
    return "Sesión expirada o no válida. Cierra sesión e inicia de nuevo.";
  }
  if (status === 429) {
    return "Demasiadas peticiones. Espera unos segundos e inténtalo de nuevo.";
  }
  return (
    (data && data.error) ||
    (typeof data === "string" ? data : "") ||
    `Error ${status} en ${path}`
  );
}

export function isUnauthorizedError(error) {
  return Boolean(error && (error.status === 401 || /401/.test(String(error.message || ""))));
}

export function mediaUrl(pathOrUrl, apiBaseUrl) {
  if (!pathOrUrl) return "";
  let value = String(pathOrUrl).trim();
  if (!value) return "";
  if (value.startsWith("http://") || value.startsWith("https://")) return value;
  const base = sanitizeBaseUrl(apiBaseUrl);
  if (value.startsWith("/")) return `${base}${value}`;
  if (value.startsWith("api/")) return `${base}/${value}`;
  return `${base}/${value}`;
}

export function mediaSrcAttr(pathOrUrl, apiBaseUrl) {
  const resolved = mediaUrl(pathOrUrl, apiBaseUrl);
  if (!resolved) return "";
  return resolved.replace(/"/g, "%22").replace(/'/g, "%27");
}
