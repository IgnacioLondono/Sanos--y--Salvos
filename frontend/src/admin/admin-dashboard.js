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
    tableSort: {},
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
    usersContainer: document.getElementById("usersContainer"),
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
      window.location.href = core.indexUrl();
      return;
    }

    if ((state.session.user.role || "").toUpperCase() !== "ADMIN") {
      core.clearSession("admin");
      window.location.href = core.indexUrl();
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
      const petBtn = event.target.closest(".js-view-pet");
      if (petBtn) {
        event.preventDefault();
        const petId = Number(petBtn.getAttribute("data-pet-id"));
        if (Number.isFinite(petId) && petId > 0) {
          openPetDetail(petId);
        }
        return;
      }
      if (event.target.closest("#btnAdminLogout")) {
        event.preventDefault();
        onLogout();
        return;
      }
      if (event.target.closest("#btnAdminRefresh")) {
        event.preventDefault();
        refreshForPage();
        return;
      }
      if (event.target.closest("#btnRefreshUsers")) {
        event.preventDefault();
        refreshUsuarios();
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

  function sortItems(items, sortOption, direction) {
    const dir = direction === "desc" ? -1 : 1;
    return [...items].sort((a, b) => {
      const av = sortOption.getter(a);
      const bv = sortOption.getter(b);
      if (sortOption.type === "number") {
        const an = Number(av);
        const bn = Number(bv);
        const safeA = Number.isFinite(an) ? an : -Infinity;
        const safeB = Number.isFinite(bn) ? bn : -Infinity;
        return (safeA - safeB) * dir;
      }
      if (sortOption.type === "date") {
        const at = new Date(av).getTime();
        const bt = new Date(bv).getTime();
        const safeA = Number.isFinite(at) ? at : 0;
        const safeB = Number.isFinite(bt) ? bt : 0;
        return (safeA - safeB) * dir;
      }
      const as = String(av || "").toLocaleLowerCase("es-CL");
      const bs = String(bv || "").toLocaleLowerCase("es-CL");
      return as.localeCompare(bs, "es", { sensitivity: "base" }) * dir;
    });
  }

  function renderTableWithSortControls(config) {
    const { tableEl, tableKey, headers, items, sortOptions, rowBuilder, limit } = config;
    if (!tableEl) return;
    const host = tableEl.closest(".table-wrap");
    if (!host) return;

    if (!state.tableSort[tableKey]) {
      state.tableSort[tableKey] = {
        key: sortOptions[0]?.key || "default",
        direction: "asc"
      };
    }

    const tableSort = state.tableSort[tableKey];
    let controls = host.querySelector(`.table-tools[data-table-key="${tableKey}"]`);
    if (!controls) {
      controls = document.createElement("div");
      controls.className = "table-tools";
      controls.setAttribute("data-table-key", tableKey);
      controls.innerHTML = `
        <label class="table-tools__label">
          Ordenar por
          <select class="table-sort-select js-table-sort-select"></select>
        </label>
        <button type="button" class="btn btn-ghost btn-sm table-sort-btn js-table-sort-toggle">A-Z</button>
      `;
      host.insertBefore(controls, tableEl);
      controls.querySelector(".js-table-sort-select")?.addEventListener("change", (event) => {
        tableSort.key = event.target.value;
        renderTableWithSortControls(config);
      });
      controls.querySelector(".js-table-sort-toggle")?.addEventListener("click", () => {
        tableSort.direction = tableSort.direction === "asc" ? "desc" : "asc";
        renderTableWithSortControls(config);
      });
    }

    const select = controls.querySelector(".js-table-sort-select");
    if (select && !select.options.length) {
      select.innerHTML = sortOptions
        .map((opt) => `<option value="${core.escapeHtml(opt.key)}">${core.escapeHtml(opt.label)}</option>`)
        .join("");
    }
    if (select) {
      select.value = tableSort.key;
    }

    const option = sortOptions.find((opt) => opt.key === tableSort.key) || sortOptions[0];
    const sorted = sortItems(items, option, tableSort.direction);
    const visible = typeof limit === "number" ? sorted.slice(0, limit) : sorted;
    const rows = visible.map((item) => rowBuilder(item));
    tableEl.innerHTML = buildTable(headers, rows);

    const directionBtn = controls.querySelector(".js-table-sort-toggle");
    if (directionBtn) {
      const isNumeric = option.type === "number" || option.type === "date";
      directionBtn.textContent = tableSort.direction === "asc" ? (isNumeric ? "Menor a mayor" : "A-Z") : isNumeric ? "Mayor a menor" : "Z-A";
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
        renderTableWithSortControls({
          tableEl: els.auditTable,
          tableKey: "audit-resumen",
          headers: ["ID", "Entidad", "Operacion", "Actor", "Fecha"],
          items: state.audit,
          sortOptions: [
            { key: "id", label: "ID", type: "number", getter: (a) => a.id },
            { key: "entidad", label: "Entidad", type: "text", getter: (a) => a.entity },
            { key: "actor", label: "Actor", type: "text", getter: (a) => a.actor },
            { key: "fecha", label: "Fecha", type: "date", getter: (a) => a.createdAt }
          ],
          rowBuilder: (a) => [a.id || "-", a.entity || "-", a.operation || "-", a.actor || "-", core.formatDate(a.createdAt)],
          limit: 15
        });
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
        renderTableWithSortControls({
          tableEl: els.zonesTable,
          tableKey: "zones",
          headers: ["ID", "Comuna", "Riesgo", "Latitud", "Longitud", "Reporte"],
          items: state.zones,
          sortOptions: [
            { key: "id", label: "ID", type: "number", getter: (z) => z.id },
            { key: "comuna", label: "Comuna", type: "text", getter: (z) => z.commune },
            { key: "riesgo", label: "Riesgo", type: "text", getter: (z) => z.riskLevel },
            { key: "reporte", label: "Reporte", type: "number", getter: (z) => z.reportId }
          ],
          rowBuilder: (z) => [
            z.id || "-",
            z.commune || "-",
            z.riskLevel || "-",
            String(z.latitude || "-"),
            String(z.longitude || "-"),
            z.reportId || "-"
          ],
          limit: 20
        });
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
      if (els.usersContainer) {
        renderUsersCards(state.users);
      }
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

  function renderUsersCards(users) {
    if (!els.usersContainer) return;
    if (!users.length) {
      els.usersContainer.innerHTML = `<p class="profile-history-empty">No hay usuarios para mostrar.</p>`;
      return;
    }
    els.usersContainer.innerHTML = users
      .map((u) => {
        const role = String(u.role || "CITIZEN").toUpperCase();
        const badgeClass = role === "ADMIN" ? "badge-admin" : "badge-primary";
        return `
          <article class="user-card">
            <div class="user-card-header">
              <div class="user-avatar"><i data-lucide="${role === "ADMIN" ? "shield" : "user"}"></i></div>
              <div class="user-info">
                <h3 class="user-name">${core.escapeHtml(u.displayName || u.fullName || u.email || "Usuario")}</h3>
                <p class="user-role">${core.escapeHtml(u.email || "—")}</p>
              </div>
              <span class="badge ${badgeClass}">${core.escapeHtml(role)}</span>
            </div>
            <div class="user-card-body">
              <div class="user-stat"><span class="user-stat-label">ID</span><span class="user-stat-value">${core.escapeHtml(String(u.id || "-"))}</span></div>
              <div class="user-stat"><span class="user-stat-label">Comuna</span><span class="user-stat-value">${core.escapeHtml(u.commune || "—")}</span></div>
              <div class="user-stat"><span class="user-stat-label">Teléfono</span><span class="user-stat-value">${core.escapeHtml(u.phone || "—")}</span></div>
              <div class="user-stat"><span class="user-stat-label">Registrado</span><span class="user-stat-value">${core.escapeHtml(core.formatDate(u.createdAt))}</span></div>
            </div>
          </article>
        `;
      })
      .join("");
    core.refreshIcons();
  }

  async function refreshReportes() {
    try {
      setStatus("Sincronizando…", false, true);
      state.reports = await core.api("/api/reports", { token: state.session.token });
      if (els.reportsTable) {
        renderReportsTable(state.reports);
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function renderReportsTable(reports) {
    renderTableWithSortControls({
      tableEl: els.reportsTable,
      tableKey: "reports",
      headers: ["ID", "Tipo", "Estado", "Comuna", "Salud", "Mascota", "Creado", "Detalle"],
      items: reports,
      sortOptions: [
        { key: "id", label: "ID", type: "number", getter: (r) => r.id },
        { key: "tipo", label: "Tipo", type: "text", getter: (r) => r.type },
        { key: "estado", label: "Estado", type: "text", getter: (r) => r.status },
        { key: "comuna", label: "Comuna", type: "text", getter: (r) => r.commune },
        { key: "fecha", label: "Fecha", type: "date", getter: (r) => r.createdAt }
      ],
      rowBuilder: (r) => {
        const petId = r.petId ? String(r.petId) : "-";
        const detailBtn =
          r.petId != null
            ? `<button class="btn btn-ghost btn-sm js-view-pet" data-pet-id="${core.escapeHtml(String(r.petId))}">Ver animal</button>`
            : "—";
        return [
          String(r.id || "-"),
          String(r.type || "-"),
          String(r.status || "-"),
          String(r.commune || "-"),
          String(r.healthStatus || "-"),
          petId,
          core.formatDate(r.createdAt),
          { html: true, value: detailBtn }
        ];
      },
      limit: 20
    });
  }

  async function openPetDetail(petId) {
    try {
      setStatus("Cargando detalle de mascota…", false, true);
      const data = await core.api(`/api/bff/pet-overview/${petId}`, { token: state.session.token });
      showPetDetailModal(data);
      setStatus("Detalle cargado.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function showPetDetailModal(data) {
    let modal = document.getElementById("petDetailModal");
    if (!modal) {
      modal = document.createElement("div");
      modal.id = "petDetailModal";
      modal.style.cssText = "position:fixed;inset:0;background:rgba(5,10,20,.65);z-index:9999;display:none;align-items:center;justify-content:center;padding:24px;";
      modal.innerHTML = `
        <div style="max-width:900px;width:100%;max-height:88vh;overflow:auto;background:#0f1b39;color:#eaf2ff;border:1px solid rgba(255,255,255,.15);border-radius:16px;padding:20px;">
          <div style="display:flex;justify-content:space-between;align-items:center;gap:10px;margin-bottom:12px;">
            <h3 style="margin:0;">Detalle del animal</h3>
            <button id="petDetailCloseBtn" class="btn btn-secondary">Cerrar</button>
          </div>
          <div id="petDetailBody"></div>
        </div>
      `;
      document.body.appendChild(modal);
      modal.addEventListener("click", (e) => {
        if (e.target === modal) modal.style.display = "none";
      });
      modal.querySelector("#petDetailCloseBtn")?.addEventListener("click", () => {
        modal.style.display = "none";
      });
    }

    const body = modal.querySelector("#petDetailBody");
    const pet = (data && data.pet) || {};
    const reports = Array.isArray(data?.reports) ? data.reports : [];
    const media = Array.isArray(data?.media) ? data.media : [];

    body.innerHTML = `
      <div style="display:grid;grid-template-columns:repeat(auto-fit,minmax(220px,1fr));gap:10px;margin-bottom:14px;">
        <div><strong>ID:</strong> ${core.escapeHtml(String(pet.id || "-"))}</div>
        <div><strong>Nombre:</strong> ${core.escapeHtml(String(pet.name || "-"))}</div>
        <div><strong>Especie:</strong> ${core.escapeHtml(String(pet.species || "-"))}</div>
        <div><strong>Raza:</strong> ${core.escapeHtml(String(pet.breed || "-"))}</div>
        <div><strong>Color:</strong> ${core.escapeHtml(String(pet.color || "-"))}</div>
        <div><strong>Tamaño:</strong> ${core.escapeHtml(String(pet.size || "-"))}</div>
        <div><strong>Chip:</strong> ${core.escapeHtml(String(pet.chipNumber || "-"))}</div>
        <div><strong>Creado:</strong> ${core.escapeHtml(core.formatDate(pet.createdAt))}</div>
      </div>
      <hr style="border-color:rgba(255,255,255,.15);margin:12px 0;">
      <p style="margin:.3rem 0;"><strong>Reportes asociados:</strong> ${reports.length}</p>
      <p style="margin:.3rem 0;"><strong>Fotos asociadas:</strong> ${media.length}</p>
      ${
        reports.length
          ? `<div style="margin-top:10px;"><strong>Últimos reportes</strong><ul>${reports
              .slice(0, 5)
              .map(
                (r) =>
                  `<li>#${core.escapeHtml(String(r.id || "-"))} - ${core.escapeHtml(
                    String(r.type || "-")
                  )} / ${core.escapeHtml(String(r.status || "-"))} / ${core.escapeHtml(
                    String(r.commune || "-")
                  )}</li>`
              )
              .join("")}</ul></div>`
          : ""
      }
    `;

    modal.style.display = "flex";
  }

  async function refreshCapacity() {
    try {
      setStatus("Sincronizando…", false, true);
      state.capacity = await core.api("/api/capacity", { token: state.session.token });
      if (els.capacityTable) {
        renderTableWithSortControls({
          tableEl: els.capacityTable,
          tableKey: "capacity",
          headers: ["ID", "Organizacion", "Voluntarios", "Horas", "Zona", "Desde"],
          items: state.capacity,
          sortOptions: [
            { key: "id", label: "ID", type: "number", getter: (c) => c.id },
            { key: "organizacion", label: "Organizacion", type: "text", getter: (c) => c.organization },
            { key: "voluntarios", label: "Voluntarios", type: "number", getter: (c) => c.volunteers },
            { key: "horas", label: "Horas", type: "number", getter: (c) => c.hoursAvailable },
            { key: "zona", label: "Zona", type: "text", getter: (c) => c.zone },
            { key: "desde", label: "Desde", type: "date", getter: (c) => c.availableFrom || c.createdAt }
          ],
          rowBuilder: (c) => [
            c.id || "-",
            c.organization || "-",
            String(c.volunteers || 0),
            String(c.hoursAvailable || 0),
            c.zone || "-",
            core.formatDate(c.availableFrom || c.createdAt)
          ],
          limit: 20
        });
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
        renderTableWithSortControls({
          tableEl: els.matchingTable,
          tableKey: "matching",
          headers: ["ID", "Reporte perdido", "Reporte encontrado", "Score", "Detalle"],
          items: state.matching,
          sortOptions: [
            { key: "id", label: "ID", type: "number", getter: (m) => m.id },
            { key: "score", label: "Score", type: "number", getter: (m) => m.score },
            { key: "perdido", label: "Reporte perdido", type: "number", getter: (m) => m.lostReportId },
            { key: "encontrado", label: "Reporte encontrado", type: "number", getter: (m) => m.foundReportId }
          ],
          rowBuilder: (m) => [
            m.id || "-",
            m.lostReportId || "-",
            m.foundReportId || "-",
            String(m.score || "0"),
            m.explanation || "-"
          ],
          limit: 20
        });
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
        renderTableWithSortControls({
          tableEl: els.auditTable,
          tableKey: "audit",
          headers: ["ID", "Entidad", "Operacion", "Actor", "Fecha"],
          items: state.audit,
          sortOptions: [
            { key: "id", label: "ID", type: "number", getter: (a) => a.id },
            { key: "entidad", label: "Entidad", type: "text", getter: (a) => a.entity },
            { key: "operacion", label: "Operacion", type: "text", getter: (a) => a.operation },
            { key: "actor", label: "Actor", type: "text", getter: (a) => a.actor },
            { key: "fecha", label: "Fecha", type: "date", getter: (a) => a.createdAt }
          ],
          rowBuilder: (a) => [a.id || "-", a.entity || "-", a.operation || "-", a.actor || "-", core.formatDate(a.createdAt)],
          limit: 20
        });
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
            (row) => {
              const tds = row
                .map((cell) => {
                  if (cell && typeof cell === "object" && cell.html) {
                    return `<td>${cell.value || ""}</td>`;
                  }
                  return `<td>${core.escapeHtml(core.truncate(cell, 72))}</td>`;
                })
                .join("");
              return `<tr>${tds}</tr>`;
            }
          )
          .join("")
      : `<tr><td colspan="${headers.length}">Sin datos disponibles.</td></tr>`;

    return `${head}<tbody>${bodyRows}</tbody>`;
  }

  function onLogout() {
    core.clearSession("admin");
    window.location.href = core.indexUrl() + "?logout=1";
  }

  async function fetchServiceHealth() {
    const checks = [
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
    const gatewayHealth = await fetchGatewayHealth(settings.apiBaseUrl);

    const services = await Promise.all(
      checks.map(async (item) => {
        try {
          const headers = {};
          headers.Authorization = `Bearer ${state.session.token}`;

          const response = await fetch(`${settings.apiBaseUrl}${item.path}`, { headers });
          if (!response.ok) {
            return { name: item.name, up: false, detail: `HTTP ${response.status}` };
          }
          let status = "UP";
          try {
            const data = await response.json();
            status = String(data.status || "UP").toUpperCase();
          } catch (e) {
            status = "UP";
          }
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

    return [gatewayHealth, ...services];
  }

  async function fetchGatewayHealth(apiBaseUrl) {
    const candidates = ["/actuator/health", "/"];
    for (const path of candidates) {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 4500);
      try {
        const response = await fetch(`${apiBaseUrl}${path}`, {
          signal: controller.signal
        });
        clearTimeout(timeoutId);
        // Considera el gateway "UP" si responde algo distinto de error 5xx.
        // Esto evita falsos negativos por auth/headers en entornos mixtos.
        if (response.status < 500) {
          return {
            name: "Gateway",
            up: true,
            detail: response.ok ? "Servicio operativo" : `Responde HTTP ${response.status}`
          };
        }
      } catch (error) {
        clearTimeout(timeoutId);
      }
    }

    return { name: "Gateway", up: false, detail: "No responde" };
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
