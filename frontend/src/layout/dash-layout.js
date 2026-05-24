(function () {
  const REDIRECTS = {
    "admin-matching.html": "admin-operaciones.html",
    "citizen-resumen.html": "citizen-reporte.html",
    "citizen-mascotas.html": "citizen-reporte.html",
    "citizen-fotos.html": "citizen-reporte.html"
  };

  const ADMIN_NAV = [
    { group: "Panel", items: [
      { href: "admin-resumen.html", icon: "layout-dashboard", label: "Resumen", page: "resumen" }
    ]},
    { group: "Gestión", items: [
      { href: "admin-usuarios.html", icon: "users", label: "Usuarios", page: "usuarios" },
      { href: "admin-reportes.html", icon: "file-text", label: "Reportes", page: "reportes" },
      { href: "admin-capacity.html", icon: "users-round", label: "Capacidad", page: "capacity" }
    ]},
    { group: "Operaciones", items: [
      { href: "admin-mapa.html", icon: "map", label: "Mapa en vivo", page: "mapa" },
      { href: "admin-operaciones.html", icon: "workflow", label: "Matching", page: "operaciones" }
    ]},
    { group: "Sistema", items: [
      { href: "admin-auditoria.html", icon: "scroll-text", label: "Auditoría", page: "auditoria" }
    ]}
  ];

  const CITIZEN_NAV = [
    { group: "Principal", items: [
      { href: "citizen-reporte.html", icon: "clipboard-plus", label: "Hacer reporte", page: "reporte" }
    ]},
    { group: "Comunidad", items: [
      { href: "citizen-mapa.html", icon: "map-pin", label: "Mapa", page: "mapa" },
      { href: "citizen-foro.html", icon: "messages-square", label: "Foro", page: "foro" },
      { href: "citizen-actividad.html", icon: "inbox", label: "Actividad", page: "actividad" }
    ]},
    { group: "Cuenta", items: [
      { href: "citizen-perfil.html", icon: "settings", label: "Mi perfil", page: "perfil" }
    ]}
  ];

  const ADMIN_TITLES = {
    resumen: "Resumen operativo",
    usuarios: "Gestión de usuarios",
    reportes: "Reportes",
    mapa: "Mapa y zonas",
    operaciones: "Matching y operaciones",
    capacity: "Capacidad y refugios",
    matching: "Matching y operaciones",
    auditoria: "Auditoría del sistema"
  };

  const CITIZEN_TITLES = {
    reporte: "Hacer reporte",
    mapa: "Mapa de la comunidad",
    mascotas: "Hacer reporte",
    fotos: "Hacer reporte",
    resumen: "Hacer reporte",
    actividad: "Actividad reciente",
    foro: "Foro de la comunidad",
    perfil: "Configuración de perfil"
  };

  function pathTo(pageFile) {
    if (window.SANOS_PATHS && typeof window.SANOS_PATHS.page === "function") {
      return window.SANOS_PATHS.page(pageFile);
    }
    return "./" + pageFile;
  }

  const curFile = (window.location.pathname.split("/").pop() || "").toLowerCase();
  if (REDIRECTS[curFile]) {
    window.location.replace(pathTo(REDIRECTS[curFile]));
    return;
  }

  function getRole() {
    const body = document.body;
    if (!body || !body.classList.contains("dash-page")) return null;
    if (body.classList.contains("admin-page") || body.dataset.adminPage) return "admin";
    if (body.classList.contains("citizen-page") || body.dataset.citizenPage) return "citizen";
    if (body.dataset.adminPage) return "admin";
    if (body.dataset.citizenPage) return "citizen";
    return null;
  }

  function getPageKey(role) {
    const body = document.body;
    if (role === "admin") return (body.dataset.adminPage || "resumen").toLowerCase();
    return (body.dataset.citizenPage || "reporte").toLowerCase();
  }

  function resolveActivePage(role, pageKey) {
    if (role === "admin") {
      if (pageKey === "matching") return "operaciones";
      return pageKey;
    }
    if (pageKey === "mascotas" || pageKey === "fotos" || pageKey === "resumen") return "reporte";
    return pageKey;
  }

  function flattenNav(sections) {
    return sections.flatMap((s) => s.items || []);
  }

  function buildNav(role, activePage) {
    const sections = role === "admin" ? ADMIN_NAV : CITIZEN_NAV;
    return sections
      .map(
        (section) => `
        <div class="dash-nav-group">
          <span class="dash-nav-group__label">${section.group}</span>
          <div class="dash-nav-group__links">
            ${(section.items || [])
              .map((item) => {
                const active = item.page === activePage;
                return (
                  `<a class="sidebar-link dash-nav__link dash-sidebar__link${active ? " is-active" : ""}" href="${item.href}"` +
                  (active ? ' aria-current="page"' : "") +
                  `><i data-lucide="${item.icon}" class="sidebar-link-icon" aria-hidden="true"></i><span>${item.label}</span></a>`
                );
              })
              .join("")}
          </div>
        </div>`
      )
      .join("");
  }

  function extractHeaderEnd(root) {
    const header = root.querySelector(".dash-header");
    if (!header) return null;
    return header.querySelector(".dash-header-end");
  }

  function buildSidebar(role, activePage, headerEnd) {
    const isAdmin = role === "admin";
    const panelTitle = isAdmin ? "Administración" : "Ciudadano";
    const panelIcon = isAdmin ? "shield" : "user-round";
    const themeBtnId = isAdmin ? "btnAdminThemeToggle" : "btnThemeToggle";
    const themeBtn = headerEnd && headerEnd.querySelector(".js-theme-toggle, .btn-theme");
    const themeHtml = themeBtn
      ? themeBtn.outerHTML
      : `<button type="button" class="btn-theme js-theme-toggle" id="${themeBtnId}" aria-label="Cambiar tema">
          <i data-lucide="moon" class="btn-theme__icon-dark" aria-hidden="true"></i>
          <i data-lucide="sun" class="btn-theme__icon-light" aria-hidden="true"></i>
        </button>`;

    const homeHref = pathTo("index.html");
    const logoSrc =
      window.SANOS_PATHS && typeof window.SANOS_PATHS.asset === "function"
        ? window.SANOS_PATHS.asset("images/logo.svg")
        : "./assets/images/logo.svg";

    return `
      <aside class="dash-sidebar" id="dashSidebar">
        <div class="dash-sidebar__inner sidebar-card">
          <a class="dash-sidebar__brand sidebar-brand" href="${homeHref}">
            <img class="brand-logo" src="${logoSrc}" alt="" width="48" height="48" />
            <div>
              <strong>Sanos y salvos</strong>
              <span>${panelTitle}</span>
            </div>
          </a>
          <nav class="dash-sidebar__nav sidebar-nav" aria-label="${isAdmin ? "Panel admin" : "Panel ciudadano"}">
            ${buildNav(role, activePage)}
          </nav>
          <div class="dash-sidebar__footer">
            ${themeHtml}
          </div>
        </div>
      </aside>
    `;
  }

  function buildTopbar(role, pageKey, headerEnd) {
    const titles = role === "admin" ? ADMIN_TITLES : CITIZEN_TITLES;
    const title = titles[pageKey] || titles[resolveActivePage(role, pageKey)] || "Panel";
    const endClone = headerEnd ? headerEnd.cloneNode(true) : document.createElement("div");
    if (!headerEnd) {
      endClone.className = "dash-header-end";
    }
    // En ciudadano, el switch de tema queda solo en la barra lateral.
    if (role === "citizen") {
      endClone.querySelectorAll(".js-theme-toggle, .btn-theme").forEach((node) => node.remove());
    }
    const navInClone = endClone.querySelector(".dash-nav");
    if (navInClone) navInClone.remove();

    return `
      <header class="dash-topbar">
        <div class="dash-topbar__start">
          <button type="button" class="btn btn-ghost dash-menu-btn" id="btnDashMenu" aria-label="Abrir menú" aria-expanded="false" aria-controls="dashSidebar">
            <i data-lucide="panel-left" aria-hidden="true"></i>
          </button>
          <div class="dash-topbar__titles">
            <span class="dash-topbar__kicker">${role === "admin" ? "Admin" : "Ciudadano"}</span>
            <h1 class="dash-topbar__title">${title}</h1>
          </div>
        </div>
        <div class="dash-topbar__end">${endClone.innerHTML}</div>
      </header>
    `;
  }

  function wireMenu() {
    const btn = document.getElementById("btnDashMenu");
    const sidebar = document.getElementById("dashSidebar");
    if (!btn || !sidebar) return;

    btn.addEventListener("click", () => {
      const open = document.body.classList.toggle("dash-sidebar-open");
      btn.setAttribute("aria-expanded", open ? "true" : "false");
    });

    document.addEventListener("click", (e) => {
      if (!document.body.classList.contains("dash-sidebar-open")) return;
      if (sidebar.contains(e.target) || btn.contains(e.target)) return;
      document.body.classList.remove("dash-sidebar-open");
      btn.setAttribute("aria-expanded", "false");
    });
  }

  function applyLayout() {
    const role = getRole();
    if (!role) return;

    const root = document.querySelector(".dash-root");
    if (!root || root.dataset.layoutReady === "1") return;

    const main = root.querySelector(".dash-main");
    if (!main) return;

    const pageKey = getPageKey(role);
    const activePage = resolveActivePage(role, pageKey);
    const headerEnd = extractHeaderEnd(root);

    const app = document.createElement("div");
    app.className = "dash-app";
    app.innerHTML =
      buildSidebar(role, activePage, headerEnd) +
      `<div class="dash-workspace">${buildTopbar(role, pageKey, headerEnd)}</div>`;

    const workspace = app.querySelector(".dash-workspace");
    workspace.appendChild(main);

    const oldHeader = root.querySelector(".dash-header");
    if (oldHeader) oldHeader.remove();

    root.innerHTML = "";
    root.appendChild(app);
    root.dataset.layoutReady = "1";
    document.body.classList.add("dash-layout-ready", role === "admin" ? "dash-role-admin" : "dash-role-citizen");

    wireMenu();

    if (window.lucide && typeof window.lucide.createIcons === "function") {
      window.lucide.createIcons();
    }
    if (window.SANOS_THEME && typeof window.SANOS_THEME.init === "function") {
      window.SANOS_THEME.init();
    }
  }

  function scrollToHash() {
    const id = (window.location.hash || "").replace("#", "");
    if (!id) return;
    const el = document.getElementById(id);
    if (el) {
      requestAnimationFrame(() => el.scrollIntoView({ behavior: "smooth", block: "start" }));
    }
  }

  function applyLayoutAndHash() {
    applyLayout();
    scrollToHash();
  }

  window.SANOS_DASH_LAYOUT = {
    init: applyLayoutAndHash,
    applyLayout: applyLayoutAndHash,
    ADMIN_NAV,
    CITIZEN_NAV
  };

  if (document.readyState === "loading") {
    document.addEventListener("DOMContentLoaded", applyLayoutAndHash);
  } else {
    applyLayoutAndHash();
  }
})();
