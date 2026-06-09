/** Utilidades de formato y sanitización HTML. */

export function escapeHtml(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

export function truncate(value, size) {
  const text = String(value || "");
  return text.length > size ? `${text.slice(0, size - 1)}...` : text;
}
