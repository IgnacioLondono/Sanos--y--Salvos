(function () {
  const core = window.SANOS_CORE;

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

    els.adminIdentity.textContent = `${state.session.user.displayName || state.session.user.email} (ADMIN)`;

    els.btnAdminRefresh.addEventListener("click", refreshAll);
    els.btnAdminLogout.addEventListener("click", onLogout);

    initMap();
    refreshAll();
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

  async function refreshAll() {
    try {
      setStatus("Sincronizando panel admin...");

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

      renderKpis();
      renderServiceHealth();
      renderTables();
      renderMap();
      setStatus("Panel admin actualizado.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function renderKpis() {
    const d = state.dashboard || {};
    const cards = [
      ["Usuarios IAM", state.users.length, "Identidades registradas"],
      ["Mascotas", d.totalPets || 0, "Catalogo total"],
      ["Reportes", d.totalReports || 0, "Eventos activos"],
      ["Capacity", d.totalCapacityRecords || 0, "Recursos declarados"],
      ["Matching", state.matching.length || 0, "Resultados de IA"],
      ["Zonas", state.zones.length || 0, "Incidencia geografica"],
      ["Auditoria", state.audit.length || 0, "Trazas de cambios"],
      ["Servicios UP", countServicesUp(), "Disponibilidad del sistema"]
    ];

    els.adminKpis.innerHTML = cards
      .map(
        (card) => `
          <article class="kpi-card">
            <span>${core.escapeHtml(card[0])}</span>
            <strong>${core.escapeHtml(String(card[1]))}</strong>
            <p class="activity-meta">${core.escapeHtml(card[2])}</p>
          </article>
        `
      )
      .join("");
  }

  function renderServiceHealth() {
    if (!state.serviceHealth.length) {
      els.serviceHealthGrid.innerHTML = '<article class="service-health-card"><strong>Sin datos</strong><p>No se pudo consultar salud de microservicios.</p></article>';
      return;
    }

    els.serviceHealthGrid.innerHTML = state.serviceHealth
      .map(
        (service) => `
          <article class="service-health-card">
            <div class="service-health-top">
              <strong>${core.escapeHtml(service.name)}</strong>
              <span class="health-dot ${service.up ? "health-up" : "health-down"}">${service.up ? "UP" : "DOWN"}</span>
            </div>
            <p>${core.escapeHtml(service.detail)}</p>
          </article>
        `
      )
      .join("");
  }

  function renderTables() {
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
    window.location.href = "./index.html";
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

  function setStatus(message, isError) {
    els.adminStatus.textContent = message;
    els.adminStatus.style.color = isError ? "#b74f4f" : "";
  }
})();
