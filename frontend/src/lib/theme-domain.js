/** Tema claro/oscuro. */

export function nextTheme(current) {
  return current === "dark" ? "light" : "dark";
}

export function normalizeTheme(mode) {
  return mode === "dark" ? "dark" : "light";
}

export function themeToggleLabel(isDark) {
  return isDark ? "Cambiar a modo claro" : "Cambiar a modo oscuro";
}
