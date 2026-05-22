(function () {
  const STORAGE_KEY = "sanos-theme";

  function current() {
    return document.documentElement.getAttribute("data-theme") === "dark" ? "dark" : "light";
  }

  function apply(mode) {
    const next = mode === "dark" ? "dark" : "light";
    document.documentElement.setAttribute("data-theme", next);
    try {
      localStorage.setItem(STORAGE_KEY, next);
    } catch (e) {
      /* ignore */
    }
    document.querySelectorAll(".js-theme-toggle").forEach(syncButton);
    if (window.lucide && typeof lucide.createIcons === "function") {
      lucide.createIcons();
    }
  }

  function syncButton(btn) {
    const dark = current() === "dark";
    btn.setAttribute("aria-pressed", dark ? "true" : "false");
    btn.title = dark ? "Cambiar a modo claro" : "Cambiar a modo oscuro";
  }

  function toggle() {
    apply(current() === "dark" ? "light" : "dark");
  }

  function bind(btn) {
    if (!btn || btn.dataset.themeBound === "1") return;
    btn.dataset.themeBound = "1";
    btn.addEventListener("click", toggle);
    syncButton(btn);
  }

  function init() {
    document.querySelectorAll(".js-theme-toggle").forEach(bind);
  }

  window.SANOS_THEME = { apply, init, toggle, current };
})();
