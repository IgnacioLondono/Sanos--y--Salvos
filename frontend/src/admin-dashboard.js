(function () {
  const core = window.SANOS_CORE;
  const PAGE = (document.body && document.body.dataset.adminPage) || "resumen";

  const state = {
    session: core.readSession("admin"),
    dashboard: null,
    serviceHealth: [],
    users: [],
    reports: [],
    capacity: [],
    zones: [],
    matching: [],
    audit: [],
    map: null,
    zonesLayer: null,
    reportsLayer: null
  };

  const els = {
    adminIdentity: document.getElementById("adminIdentity"),
    adminStatus: document.getElementById("adminStatus"),
    adminKpis: document.getElementById("adminKpis"),
    serviceHealthGrid: document.getElementById("serviceHealthGrid"),
    usersTable: document.getElementById("usersTable"),
    reportsTable: document.getElementById("reportsTable"),
    capacityTable: document.getElementById("capacityTable"),
    zonesTable: document.getElementById("zonesTable"),
    matchingTable: document.getElementById("matchingTable"),
    auditTable: document.getElementById("auditTable"),
    btnAdminRefresh: document.getElementById("btnAdminRefresh"),
    btnAdminLogout: document.getElementById("btnAdminLogout")
  };

  init();

  function init() {
    if (!state.session.token || !state.session.user) {
      window.location.href = "./index.html";
      return;
    }

    if ((state.session.user.role || "").toUpperCase() !== "ADMIN") {
      core.clearSession("admin");
      window.location.href = "./index.html";
      return;
    }

    if (window.SANOS_DASH_LAYOUT && typeof window.SANOS_DASH_LAYOUT.applyLayout === "function") {
      window.SANOS_DASH_LAYOUT.applyLayout();
    }

    wireAdminActions();
    syncAdminIdentity();
    wireAdminNav();
    refreshForPage();
  }

  function wireAdminActions() {
    document.addEventListener("click", (event) => {
      if (event.target.closest("#btnAdminLogout")) {
        event.preventDefault();
        onLogout();
        return;
      }
      if (event.target.closest("#btnAdminRefresh")) {
        event.preventDefault();
        refreshForPage();
      }
    });
  }

  function syncAdminIdentity() {
    const name = state.session.user.displayName || state.session.user.email;
    const node = document.getElementById("adminIdentity");
    if (node) node.textContent = name;
  }

  function wireAdminNav() {
    const cur = (window.location.pathname.split("/").pop() || "").toLowerCase();
    const alias =
      cur === "admin-matching.html" ? "admin-operaciones.html" : cur;
    document.querySelectorAll(".dash-nav__link[href], .dash-sidebar__link[href]").forEach((a) => {
      const href = (a.getAttribute("href") || "").toLowerCase();
      const match =
        href === alias ||
        (alias === "admin-dashboard.html" && href === "admin-resumen.html");
      a.classList.toggle("is-active", match);
      if (match) a.setAttribute("aria-current", "page");
      else a.removeAttribute("aria-current");
    });
  }

  function refreshForPage() {
    if (PAGE === "resumen") return refreshResumen();
    if (PAGE === "mapa") {
      initMap();
      return refreshMapa();
    }
    if (PAGE === "usuarios") return refreshUsuarios();
    if (PAGE === "reportes") return refreshReportes();
    if (PAGE === "operaciones" || PAGE === "capacity" || PAGE === "matching") return refreshOperaciones();
    if (PAGE === "auditoria") return refreshAuditoria();
  }

  function initMap() {
    if (typeof L === "undefined") return;
    const settings = core.loadSettings();
    const center = settings.defaultCenter || { lat: -33.4489, lng: -70.6693 };
    state.map = L.map("adminMap").setView([center.lat, center.lng], 11);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap"
    }).addTo(state.map);
    state.zonesLayer = L.layerGroup().addTo(state.map);
    state.reportsLayer = L.layerGroup().addTo(state.map);
  }

  function riskColor(level) {
    const l = String(level || "").toUpperCase();
    if (l.includes("ALT") || l === "HIGH") return "#c0392b";
    if (l.includes("MED") || l === "MEDIUM") return "#f1b14c";
    return "#3d8f73";
  }

  function renderMap() {
    if (!state.map) return;
    state.zonesLayer.clearLayers();
    state.reportsLayer.clearLayers();
    const bounds = [];

    state.zones.forEach((zone) => {
      if (zone.latitude == null || zone.longitude == null) return;
      const lat = Number(zone.latitude);
      const lng = Number(zone.longitude);
      if (Number.isNaN(lat) || Number.isNaN(lng)) return;
      const color = riskColor(zone.riskLevel);
      const circle = L.circle([lat, lng], {
        radius: 800,
        color,
        fillColor: color,
        fillOpacity: 0.25,
        weight: 2
      });
      circle.bindPopup(
        `<strong>Zona ${core.escapeHtml(zone.commune || "")}</strong><br/>` +
          `Riesgo: ${core.escapeHtml(zone.riskLevel || "-")}<br/>` +
          `Reporte vinculado: ${core.escapeHtml(String(zone.reportId || "-"))}`
      );
      circle.addTo(state.zonesLayer);
      bounds.push([lat, lng]);
    });

    state.reports.forEach((report) => {
      if (report.latitude == null || report.longitude == null) return;
      const lat = Number(report.latitude);
      const lng = Number(report.longitude);
      if (Number.isNaN(lat) || Number.isNaN(lng)) return;
      const isLost = String(report.type || "").toUpperCase() === "LOST";
      const color = isLost ? "#c0392b" : "#3d8f73";
      const marker = L.circleMarker([lat, lng], {
        radius: 7,
        color,
        fillColor: color,
        fillOpacity: 0.85
      });
      marker.bindPopup(
        `<strong>${core.escapeHtml(report.type || "Reporte")} #${core.escapeHtml(String(report.id || "-"))}</strong><br/>` +
          `${core.escapeHtml(report.commune || "Sin comuna")} - ${core.escapeHtml(report.status || "-")}<br/>` +
          `<em>${core.escapeHtml(report.description || "Sin descripcion")}</em>`
      );
      marker.addTo(state.reportsLayer);
      bounds.push([lat, lng]);
    });

    if (bounds.length) {
      state.map.fitBounds(bounds, { padding: [30, 30], maxZoom: 13 });
    }
  }

  async function refreshResumen() {
    try {
      setStatus("Sincronizando…", false, true);

      const [dashboard, users, reports, capacity, zones, matching, audit, serviceHealth] = await Promise.all([
        core.api("/api/bff/dashboard", { token: state.session.token }),
        core.api("/api/iam/users", { token: state.session.token }),
        core.api("/api/reports", { token: state.session.token }),
        core.api("/api/capacity", { token: state.session.token }),
        core.api("/api/zones", { token: state.session.token }),
        core.api("/api/matching", { token: state.session.token }),
        core.api("/api/audit", { token: state.session.token }),
        fetchServiceHealth()
      ]);

      state.dashboard = dashboard;
      state.users = users;
      state.reports = reports;
      state.capacity = capacity;
      state.zones = zones;
      state.matching = matching;
      state.audit = audit;
      state.serviceHealth = serviceHealth;

      if (els.adminKpis) renderKpis();
      if (els.serviceHealthGrid) renderServiceHealth();
      if (els.auditTable) {
        els.auditTable.innerHTML = buildTable(
          ["ID", "Entidad", "Operacion", "Actor", "Fecha"],
          state.audit.slice(0, 15).map((a) => [
            a.id || "-",
            a.entity || "-",
            a.operation || "-",
            a.actor || "-",
            core.formatDate(a.createdAt)
          ])
        );
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshMapa() {
    try {
      setStatus("Sincronizando…", false, true);
      const [zones, reports] = await Promise.all([
        core.api("/api/zones", { token: state.session.token }),
        core.api("/api/reports", { token: state.session.token })
      ]);
      state.zones = zones;
      state.reports = reports;
      if (els.zonesTable) {
        els.zonesTable.innerHTML = buildTable(
          ["ID", "Comuna", "Riesgo", "Latitud", "Longitud", "Reporte"],
          state.zones.slice(0, 20).map((z) => [
            z.id || "-",
            z.commune || "-",
            z.riskLevel || "-",
            String(z.latitude || "-"),
            String(z.longitude || "-"),
            z.reportId || "-"
          ])
        );
      }
      renderMap();
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshUsuarios() {
    try {
      setStatus("Sincronizando…", false, true);
      state.users = await core.api("/api/iam/users", { token: state.session.token });
      if (els.usersTable) {
        els.usersTable.innerHTML = buildTable(
          ["ID", "Email", "Nombre", "Rol", "Creado"],
          state.users.map((u) => [
            u.id || "-",
            u.email || "-",
            u.displayName || "-",
            u.role || "-",
            core.formatDate(u.createdAt)
          ])
        );
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshReportes() {
    try {
      setStatus("Sincronizando…", false, true);
      state.reports = await core.api("/api/reports", { token: state.session.token });
      if (els.reportsTable) {
        els.reportsTable.innerHTML = buildTable(
          ["ID", "Tipo", "Estado", "Comuna", "Salud", "Creado"],
          state.reports.slice(0, 20).map((r) => [
            r.id || "-",
            r.type || "-",
            r.status || "-",
            r.commune || "-",
            r.healthStatus || "-",
            core.formatDate(r.createdAt)
          ])
        );
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshCapacity() {
    try {
      setStatus("Sincronizando…", false, true);
      state.capacity = await core.api("/api/capacity", { token: state.session.token });
      if (els.capacityTable) {
        els.capacityTable.innerHTML = buildTable(
          ["ID", "Organizacion", "Voluntarios", "Horas", "Zona", "Desde"],
          state.capacity.slice(0, 20).map((c) => [
            c.id || "-",
            c.organization || "-",
            String(c.volunteers || 0),
            String(c.hoursAvailable || 0),
            c.zone || "-",
            core.formatDate(c.availableFrom || c.createdAt)
          ])
        );
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshOperaciones() {
    await Promise.all([refreshCapacity(), refreshMatching()]);
  }

  async function refreshMatching() {
    try {
      setStatus("Sincronizando…", false, true);
      state.matching = await core.api("/api/matching", { token: state.session.token });
      if (els.matchingTable) {
        els.matchingTable.innerHTML = buildTable(
          ["ID", "Reporte perdido", "Reporte encontrado", "Score", "Detalle"],
          state.matching.slice(0, 20).map((m) => [
            m.id || "-",
            m.lostReportId || "-",
            m.foundReportId || "-",
            String(m.score || "0"),
            m.explanation || "-"
          ])
        );
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshAuditoria() {
    try {
      setStatus("Sincronizando…", false, true);
      state.audit = await core.api("/api/audit", { token: state.session.token });
      if (els.auditTable) {
        els.auditTable.innerHTML = buildTable(
          ["ID", "Entidad", "Operacion", "Actor", "Fecha"],
          state.audit.slice(0, 20).map((a) => [
            a.id || "-",
            a.entity || "-",
            a.operation || "-",
            a.actor || "-",
            core.formatDate(a.createdAt)
          ])
        );
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function renderKpis() {
    if (!els.adminKpis) return;
    const d = state.dashboard || {};
    const cards = [
      { label: "Usuarios", value: state.users.length, icon: "users", tint: "kpi-ico-indigo" },
      { label: "Mascotas", value: d.totalPets || 0, icon: "paw-print", tint: "kpi-ico-mint" },
      { label: "Reportes", value: d.totalReports || 0, icon: "file-text", tint: "kpi-ico-gold" },
      { label: "Capacity", value: d.totalCapacityRecords || 0, icon: "users-round", tint: "kpi-ico-sky" },
      { label: "Matching", value: state.matching.length || 0, icon: "sparkles", tint: "kpi-ico-violet" },
      { label: "Zonas", value: state.zones.length || 0, icon: "map-pin", tint: "kpi-ico-rose" },
      { label: "Auditoria", value: state.audit.length || 0, icon: "history", tint: "kpi-ico-indigo" },
      { label: "Servicios OK", value: countServicesUp(), icon: "server", tint: "kpi-ico-mint" }
    ];

    els.adminKpis.innerHTML = cards
      .map(
        (c) => `
          <article class="kpi-card kpi-card--modern">
            <div class="kpi-card__row">
              <div class="kpi-card__ico ${c.tint}"><i data-lucide="${c.icon}"></i></div>
              <div>
                <span class="kpi-label">${core.escapeHtml(c.label)}</span>
                <strong>${core.escapeHtml(String(c.value))}</strong>
              </div>
            </div>
          </article>
        `
      )
      .join("");
    core.refreshIcons();
  }

  function renderServiceHealth() {
    if (!els.serviceHealthGrid) return;
    if (!state.serviceHealth.length) {
      els.serviceHealthGrid.innerHTML = `<span class="health-chip health-chip--down"><i data-lucide="wifi-off"></i>${core.escapeHtml("Sin datos")}</span>`;
      core.refreshIcons();
      return;
    }

    els.serviceHealthGrid.innerHTML = state.serviceHealth
      .map(
        (service) => `
          <span class="health-chip ${service.up ? "health-chip--up" : "health-chip--down"}" title="${core.escapeHtml(service.detail)}">
            <i data-lucide="${service.up ? "check-circle" : "x-circle"}"></i>
            ${core.escapeHtml(service.name)}
          </span>
        `
      )
      .join("");
    core.refreshIcons();
  }

  function buildTable(headers, rows) {
    const head = `
      <thead>
        <tr>${headers.map((h) => `<th>${core.escapeHtml(h)}</th>`).join("")}</tr>
      </thead>
    `;

    const bodyRows = rows.length
      ? rows
          .map(
            (row) =>
              `<tr>${row.map((cell) => `<td>${core.escapeHtml(core.truncate(cell, 72))}</td>`).join("")}</tr>`
          )
          .join("")
      : `<tr><td colspan="${headers.length}">Sin datos disponibles.</td></tr>`;

    return `${head}<tbody>${bodyRows}</tbody>`;
  }

  function onLogout() {
    core.clearSession("admin");
    window.location.href = "./index.html?logout=1";
  }

  async function fetchServiceHealth() {
    const checks = [
      { name: "Gateway", path: "/actuator/health", publicEndpoint: true },
      { name: "BFF", path: "/api/bff/health" },
      { name: "IAM", path: "/api/iam/health" },
      { name: "Catalogo Mascotas", path: "/api/pets/health" },
      { name: "Reportes", path: "/api/reports/health" },
      { name: "Geo Inteligencia", path: "/api/zones/health" },
      { name: "Media", path: "/api/media/health" },
      { name: "Matching IA", path: "/api/matching/health" },
      { name: "Capacity", path: "/api/capacity/health" },
      { name: "Auditoria", path: "/api/audit/health" }
    ];

    const settings = core.loadSettings();

    return Promise.all(
      checks.map(async (item) => {
        try {
          const headers = {};
          if (!item.publicEndpoint) {
            headers.Authorization = `Bearer ${state.session.token}`;
          }

          const response = await fetch(`${settings.apiBaseUrl}${item.path}`, { headers });
          if (!response.ok) {
            return { name: item.name, up: false, detail: `HTTP ${response.status}` };
          }

          const data = await response.json();
          const status = String(data.status || "UP").toUpperCase();
          return {
            name: item.name,
            up: status === "UP",
            detail: status === "UP" ? "Servicio operativo" : `Estado ${status}`
          };
        } catch (error) {
          return { name: item.name, up: false, detail: "No responde" };
        }
      })
    );
  }

  function countServicesUp() {
    return state.serviceHealth.filter((service) => service.up).length;
  }

  function statusNode() {
    return document.querySelector(".dash-topbar #adminStatus") || document.getElementById("adminStatus");
  }

  function setStatus(message, isError, loading) {
    const node = statusNode();
    if (!node) return;
    const esc = core.escapeHtml(message);
    const left = loading
      ? `<span class="lucide-spin"><i data-lucide="loader"></i></span>`
      : `<i data-lucide="${isError ? "alert-circle" : "check"}"></i>`;
    node.className = `sync-pill${isError ? " is-error" : ""}`;
    node.innerHTML = `${left}<span>${esc}</span>`;
    core.refreshIcons();
  }
})();
