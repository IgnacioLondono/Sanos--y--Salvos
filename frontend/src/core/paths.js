/**
 * Rutas relativas segun si la pagina esta en /pages/* o en la raiz del frontend.
 */
(function () {
  function inPages() {
    return window.location.pathname.includes("/pages/");
  }

  function rootPrefix() {
    return inPages() ? "../../" : "./";
  }

  function page(file) {
    if (!file) return rootPrefix();
    if (file === "index.html" || file === "register.html") {
      return rootPrefix() + file;
    }
    if (inPages()) {
      if (file.startsWith("admin-") && !window.location.pathname.includes("/pages/admin/")) {
        return rootPrefix() + "pages/admin/" + file;
      }
      if (file.startsWith("citizen-") && !window.location.pathname.includes("/pages/citizen/")) {
        return rootPrefix() + "pages/citizen/" + file;
      }
      return file;
    }
    if (file.startsWith("admin-")) return "./pages/admin/" + file;
    if (file.startsWith("citizen-")) return "./pages/citizen/" + file;
    return "./" + file;
  }

  window.SANOS_PATHS = {
    inPages,
    root: rootPrefix,
    asset: function (sub) {
      return rootPrefix() + "assets/" + sub;
    },
    page,
    index: function () {
      return rootPrefix() + "index.html";
    },
    register: function () {
      return rootPrefix() + "register.html";
    }
  };
})();
