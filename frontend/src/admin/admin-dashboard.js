(function () {
  const core = window.SANOS_CORE;
  const PAGE = (document.body && document.body.dataset.adminPage) || "resumen";

  const state = {
    session: core.readSession("admin"),
    dashboard: null,
    serviceHealth: [],
    users: [],
    reports: [],
    media: [],
    pets: [],
    capacity: [],
    zones: [],
    matching: [],
    audit: [],
    tableSort: {},
    usersFilter: "",
    userModal: { user: null, reports: [], mediaByReport: {} },
    mapCtrl: null
  };

  const els = {
    adminIdentity: document.getElementById("adminIdentity"),
    adminStatus: document.getElementById("adminStatus"),
    adminKpis: document.getElementById("adminKpis"),
    adminDonuts: document.getElementById("adminDonuts"),
    serviceHealthGrid: document.getElementById("serviceHealthGrid"),
    usersTable: document.getElementById("usersTable"),
    usersContainer: document.getElementById("usersContainer"),
    usersSearch: document.getElementById("usersSearch"),
    userReportsModal: document.getElementById("userReportsModal"),
    userReportsList: document.getElementById("userReportsList"),
    userReportsEmpty: document.getElementById("userReportsEmpty"),
    userReportsTitle: document.getElementById("userReportsTitle"),
    userReportsEmail: document.getElementById("userReportsEmail"),
    userReportsRole: document.getElementById("userReportsRole"),
    userReportsSearch: document.getElementById("userReportsSearch"),
    userReportsType: document.getElementById("userReportsType"),
    userReportsStatus: document.getElementById("userReportsStatus"),
    userReportsCommune: document.getElementById("userReportsCommune"),
    btnCloseUserReports: document.getElementById("btnCloseUserReports"),
    createAdminModal: document.getElementById("createAdminModal"),
    createAdminForm: document.getElementById("createAdminForm"),
    btnOpenCreateAdmin: document.getElementById("btnOpenCreateAdmin"),
    btnCloseCreateAdmin: document.getElementById("btnCloseCreateAdmin"),
    btnCancelCreateAdmin: document.getElementById("btnCancelCreateAdmin"),
    adminFullName: document.getElementById("adminFullName"),
    adminRut: document.getElementById("adminRut"),
    adminEmail: document.getElementById("adminEmail"),
    adminPassword: document.getElementById("adminPassword"),
    adminPasswordConfirm: document.getElementById("adminPasswordConfirm"),
    adminCommune: document.getElementById("adminCommune"),
    adminPhone: document.getElementById("adminPhone"),
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
    const adminSession = core.readSession("admin");
    const citizenSession = core.readSession("citizen");
    const adminRole = (adminSession.user && adminSession.user.role || "").toUpperCase();
    const hasValidAdmin =
      adminSession.token && adminSession.user && adminRole === "ADMIN";

    if (hasValidAdmin) {
      state.session = adminSession;
    } else if (citizenSession.token && citizenSession.user) {
      window.location.replace(core.forbiddenUrl());
      return;
    } else if (!adminSession.token || !adminSession.user) {
      window.location.href = core.indexUrl();
      return;
    } else {
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
    wireUsersPage();
    refreshForPage();
  }

  function wireUsersPage() {
    if (PAGE !== "usuarios") return;

    if (els.usersSearch) {
      els.usersSearch.addEventListener("input", () => {
        state.usersFilter = els.usersSearch.value.trim().toLowerCase();
        renderUsersCards(state.users);
      });
    }

    ["userReportsSearch", "userReportsType", "userReportsStatus", "userReportsCommune"].forEach((key) => {
      const node = els[key];
      if (!node) return;
      const evt = node.tagName === "SELECT" ? "change" : "input";
      node.addEventListener(evt, () => renderUserReportsList());
    });

    if (els.btnCloseUserReports) {
      els.btnCloseUserReports.addEventListener("click", closeUserReportsModal);
    }
    if (els.userReportsModal) {
      els.userReportsModal.addEventListener("click", (e) => {
        if (e.target === els.userReportsModal) closeUserReportsModal();
      });
    }
    if (els.btnOpenCreateAdmin) {
      els.btnOpenCreateAdmin.addEventListener("click", openCreateAdminModal);
    }
    if (els.btnCloseCreateAdmin) {
      els.btnCloseCreateAdmin.addEventListener("click", closeCreateAdminModal);
    }
    if (els.btnCancelCreateAdmin) {
      els.btnCancelCreateAdmin.addEventListener("click", closeCreateAdminModal);
    }
    if (els.createAdminForm) {
      els.createAdminForm.addEventListener("submit", onCreateAdminSubmit);
    }
    if (els.createAdminModal) {
      els.createAdminModal.addEventListener("click", (e) => {
        if (e.target === els.createAdminModal) closeCreateAdminModal();
      });
    }

    document.addEventListener("keydown", (e) => {
      if (e.key !== "Escape") return;
      if (els.createAdminModal && !els.createAdminModal.classList.contains("hidden")) {
        closeCreateAdminModal();
        return;
      }
      if (els.userReportsModal && !els.userReportsModal.classList.contains("hidden")) {
        closeUserReportsModal();
      }
    });
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
        return;
      }
      const deleteReportBtn = event.target.closest(".js-delete-report");
      if (deleteReportBtn) {
        event.preventDefault();
        event.stopPropagation();
        const reportId = Number(deleteReportBtn.getAttribute("data-report-id"));
        if (Number.isFinite(reportId) && reportId > 0) {
          deleteUserReport(reportId);
        }
        return;
      }
      const userCard = event.target.closest(".js-user-card");
      if (userCard) {
        event.preventDefault();
        const userId = Number(userCard.getAttribute("data-user-id"));
        if (Number.isFinite(userId) && userId > 0) {
          openUserReportsModal(userId);
        }
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
      return initMap().then(() => refreshMapa());
    }
    if (PAGE === "usuarios") return refreshUsuarios();
    if (PAGE === "reportes") return refreshReportes();
    if (PAGE === "operaciones" || PAGE === "capacity" || PAGE === "matching") return refreshOperaciones();
    if (PAGE === "auditoria") return refreshAuditoria();
  }

  async function initMap() {
    const container = document.getElementById("adminMap");
    if (!container || !window.SANOS_MAPS) return;
    if (state.mapCtrl) {
      requestAnimationFrame(() => state.mapCtrl.invalidateSize());
      return;
    }
    const settings = core.loadSettings();
    const center = settings.defaultCenter || { lat: -33.4489, lng: -70.6693 };
    try {
      state.mapCtrl = await window.SANOS_MAPS.createMap(container, {
        center,
        zoom: 11
      });
    } catch (err) {
      setStatus(err.message || "Mapa no disponible", true);
    }
  }

  function riskColor(level) {
    const l = String(level || "").toUpperCase();
    if (l.includes("ALT") || l === "HIGH") return "#c0392b";
    if (l.includes("MED") || l === "MEDIUM") return "#f1b14c";
    return "#3d8f73";
  }

  function renderMap() {
    if (!state.mapCtrl) return;
    state.mapCtrl.clearMarkers();
    state.mapCtrl.clearCircles();
    const bounds = [];

    state.zones.forEach((zone) => {
      if (zone.latitude == null || zone.longitude == null) return;
      const lat = Number(zone.latitude);
      const lng = Number(zone.longitude);
      if (Number.isNaN(lat) || Number.isNaN(lng)) return;
      const color = riskColor(zone.riskLevel);
      state.mapCtrl.addCircle(lat, lng, {
        color,
        radius: 800,
        popupHtml: core.buildMapZonePopup(zone.commune, zone.riskLevel, zone.reportId)
      });
      bounds.push([lat, lng]);
    });

    const mediaIndex = core.indexMediaByReportAndPet(state.media);

    state.reports.forEach((report) => {
      if (report.latitude == null || report.longitude == null) return;
      const lat = Number(report.latitude);
      const lng = Number(report.longitude);
      if (Number.isNaN(lat) || Number.isNaN(lng)) return;
      const isLost = String(report.type || "").toUpperCase() === "LOST";
      const color = isLost ? "#c0392b" : "#3d8f73";
      const petName = report.petId ? core.petNameById(report.petId, state.pets) : "";
      state.mapCtrl.addMarker(lat, lng, {
        color,
        scale: 7,
        popupHtml: core.buildMapReportPopup(report, {
          petName,
          imageUrl: mediaIndex.imageForReport(report),
          showId: true
        })
      });
      bounds.push([lat, lng]);
    });

    if (bounds.length) {
      state.mapCtrl.fitPoints(bounds, 40);
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
      if (els.adminDonuts) renderDonuts();
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
      const [zones, reports, media, pets] = await Promise.all([
        core.api("/api/zones", { token: state.session.token }),
        core.api("/api/reports", { token: state.session.token }),
        core.api("/api/media", { auth: false }).catch(() => []),
        core.api("/api/pets", { auth: false }).catch(() => [])
      ]);
      state.zones = zones;
      state.reports = reports;
      state.media = media;
      state.pets = pets;
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
      if (els.reportsTable) {
        renderReportsTable(state.reports);
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
      const [users, reports, media] = await Promise.all([
        core.api("/api/iam/users", { token: state.session.token }),
        core.api("/api/reports", { auth: false }),
        core.api("/api/media", { auth: false })
      ]);
      state.users = users;
      state.reports = reports;
      state.media = media;
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

  function reportCountForUser(userId) {
    const id = Number(userId);
    if (!Number.isFinite(id)) return 0;
    return (state.reports || []).filter((r) => Number(r.createdBy) === id).length;
  }

  function mediaForReport(report) {
    const reportId = Number(typeof report === "object" ? report.id : report);
    if (!Number.isFinite(reportId)) return [];

    const cached = state.userModal.mediaByReport && state.userModal.mediaByReport[reportId];
    if (Array.isArray(cached)) {
      return cached.filter((m) => m && mediaItemUrl(m));
    }

    const byReport = (state.media || []).filter((m) => Number(m.reportId) === reportId);
    if (byReport.length) return byReport.filter((m) => mediaItemUrl(m));
    const petId = Number(typeof report === "object" ? report.petId : NaN);
    if (!Number.isFinite(petId)) return [];
    return (state.media || []).filter((m) => Number(m.petId) === petId && mediaItemUrl(m));
  }

  function mediaItemUrl(item) {
    if (!item) return "";
    return item.url || item.publicUrl || item.storageUrl || "";
  }

  async function loadModalMedia(reports) {
    const map = {};
    await Promise.all(
      (reports || []).map(async (report) => {
        const reportId = Number(report.id);
        if (!Number.isFinite(reportId)) return;
        const collected = [];
        const seen = new Set();

        const addItems = (items) => {
          (items || []).forEach((item) => {
            const url = mediaItemUrl(item);
            const key = item && item.id != null ? `id:${item.id}` : `url:${url}`;
            if (!url || seen.has(key)) return;
            seen.add(key);
            collected.push({ ...item, url });
          });
        };

        try {
          addItems(await core.api(`/api/media/report/${reportId}`, { auth: false }));
        } catch (error) {
          /* sin fotos por reporte */
        }

        const petId = Number(report.petId);
        if (Number.isFinite(petId)) {
          try {
            addItems(await core.api(`/api/media/pet/${petId}`, { auth: false }));
          } catch (error) {
            /* sin fotos por mascota */
          }
        }

        map[reportId] = collected;
      })
    );
    state.userModal.mediaByReport = map;
  }

  function renderReportThumb(report, photos) {
    const valid = (photos || []).filter((p) => mediaItemUrl(p));
    if (!valid.length) {
      return `<div class="user-report-item__thumb user-report-item__thumb--empty" aria-label="Sin foto">
        <i data-lucide="image-off"></i>
        <span>Sin foto</span>
      </div>`;
    }

    const src = core.mediaSrcAttr(mediaItemUrl(valid[0]));
    return `<img class="user-report-item__thumb" src="${src}" alt="Evidencia reporte ${core.escapeHtml(String(report.id))}" loading="lazy" decoding="async" onerror="this.replaceWith(Object.assign(document.createElement('div'),{className:'user-report-item__thumb user-report-item__thumb--empty',innerHTML:'<span>Sin foto</span>'}))" />`;
  }

  function filterUsersList(users) {
    const q = state.usersFilter.trim().toLowerCase();
    if (!q) return users;
    return users.filter((u) => {
      const haystack = [u.displayName, u.fullName, u.email, u.commune, u.role, String(u.id || "")]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
      return haystack.includes(q);
    });
  }

  function renderUsersCards(users) {
    if (!els.usersContainer) return;
    const list = filterUsersList(users || []);
    if (!list.length) {
      els.usersContainer.innerHTML = `<p class="profile-history-empty">No hay usuarios para mostrar.</p>`;
      return;
    }
    els.usersContainer.innerHTML = list
      .map((u) => {
        const role = String(u.role || "CITIZEN").toUpperCase();
        const badgeClass = role === "ADMIN" ? "badge-admin" : "badge-primary";
        const reportCount = reportCountForUser(u.id);
        const avatarIcon = role === "ADMIN" ? "shield-check" : "user-round";
        return `
          <button type="button" class="user-card js-user-card" data-user-id="${core.escapeHtml(String(u.id))}">
            <div class="user-card-header">
              <div class="user-avatar"><i data-lucide="${avatarIcon}"></i></div>
              <div class="user-info">
                <h3 class="user-name">${core.escapeHtml(u.displayName || u.fullName || u.email || "Usuario")}</h3>
                <p class="user-role">${core.escapeHtml(u.email || "—")}</p>
              </div>
              <span class="badge ${badgeClass}">${core.escapeHtml(role)}</span>
            </div>
            <div class="user-card-body">
              <div class="user-stat"><span class="user-stat-label">ID</span><span class="user-stat-value">${core.escapeHtml(String(u.id || "-"))}</span></div>
              <div class="user-stat"><span class="user-stat-label">Comuna</span><span class="user-stat-value">${core.escapeHtml(u.commune || "—")}</span></div>
              <div class="user-stat"><span class="user-stat-label">Reportes</span><span class="user-stat-value">${reportCount}</span></div>
              <div class="user-stat"><span class="user-stat-label">Registrado</span><span class="user-stat-value">${core.escapeHtml(core.formatDate(u.createdAt))}</span></div>
            </div>
            <p class="user-card-hint"><i data-lucide="clipboard-list"></i> Ver reportes</p>
          </button>
        `;
      })
      .join("");
    core.refreshIcons();
  }

  function closeUserReportsModal() {
    if (!els.userReportsModal) return;
    els.userReportsModal.classList.add("hidden");
    els.userReportsModal.setAttribute("aria-hidden", "true");
    state.userModal = { user: null, reports: [], mediaByReport: {} };
  }

  async function openUserReportsModal(userId) {
    const user = state.users.find((u) => Number(u.id) === Number(userId));
    if (!user || !els.userReportsModal) return;

    try {
      setStatus("Cargando reportes…", false, true);
      const reports = await core.api(`/api/reports/user/${userId}`, { auth: false });
      state.userModal = { user, reports: Array.isArray(reports) ? reports : [], mediaByReport: {} };
      await loadModalMedia(state.userModal.reports);

      if (els.userReportsTitle) {
        els.userReportsTitle.textContent = user.displayName || user.fullName || user.email || "Usuario";
      }
      if (els.userReportsEmail) els.userReportsEmail.textContent = user.email || "—";
      if (els.userReportsRole) {
        const role = String(user.role || "CITIZEN").toUpperCase();
        els.userReportsRole.textContent = role;
        els.userReportsRole.className = `badge ${role === "ADMIN" ? "badge-admin" : "badge-primary"}`;
      }
      if (els.userReportsSearch) els.userReportsSearch.value = "";
      if (els.userReportsType) els.userReportsType.value = "";
      if (els.userReportsStatus) els.userReportsStatus.value = "";
      if (els.userReportsCommune) els.userReportsCommune.value = "";

      renderUserReportsList();

      els.userReportsModal.classList.remove("hidden");
      els.userReportsModal.setAttribute("aria-hidden", "false");
      core.refreshIcons();
      setStatus("Reportes cargados.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function openCreateAdminModal() {
    if (!els.createAdminModal) return;
    if (els.createAdminForm) els.createAdminForm.reset();
    els.createAdminModal.classList.remove("hidden");
    els.createAdminModal.setAttribute("aria-hidden", "false");
    if (els.adminFullName) els.adminFullName.focus();
    core.refreshIcons();
  }

  function closeCreateAdminModal() {
    if (!els.createAdminModal) return;
    els.createAdminModal.classList.add("hidden");
    els.createAdminModal.setAttribute("aria-hidden", "true");
  }

  async function onCreateAdminSubmit(event) {
    event.preventDefault();
    const password = els.adminPassword ? els.adminPassword.value : "";
    const passwordConfirm = els.adminPasswordConfirm ? els.adminPasswordConfirm.value : "";
    const payload = {
      fullName: (els.adminFullName && els.adminFullName.value.trim()) || "",
      rutDocument: (els.adminRut && els.adminRut.value.trim()) || "",
      email: (els.adminEmail && els.adminEmail.value.trim()) || "",
      password,
      commune: (els.adminCommune && els.adminCommune.value.trim()) || "Santiago",
      phone: (els.adminPhone && els.adminPhone.value.trim()) || ""
    };

    if (!payload.fullName || !payload.rutDocument || !payload.email || !password || !passwordConfirm) {
      return setStatus("Completa nombre, RUT, correo y ambas contraseñas.", true);
    }
    if (password.length < 10) {
      return setStatus("La contraseña debe tener al menos 10 caracteres.", true);
    }
    if (password !== passwordConfirm) {
      return setStatus("Las contraseñas no coinciden.", true);
    }

    try {
      setStatus("Creando administrador…", false, true);
      await core.api("/api/iam/admin/users", {
        method: "POST",
        token: state.session.token,
        body: payload
      });
      closeCreateAdminModal();
      await refreshUsuarios();
      setStatus("Administrador creado correctamente.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function reportTypeLabel(type) {
    const t = String(type || "").toUpperCase();
    if (t === "LOST" || t === "PERDIDA") return "Perdida";
    if (t === "FOUND" || t === "ENCONTRADA") return "Encontrada";
    return type || "—";
  }

  function reportStatusLabel(status) {
    const s = String(status || "").toUpperCase();
    if (s === "OPEN" || s === "ABIERTO") return "Abierto";
    if (s === "CLOSED" || s === "CERRADO") return "Cerrado";
    return status || "—";
  }

  function filteredUserReports() {
    const reports = state.userModal.reports || [];
    const q = (els.userReportsSearch && els.userReportsSearch.value.trim().toLowerCase()) || "";
    const type = (els.userReportsType && els.userReportsType.value) || "";
    const status = (els.userReportsStatus && els.userReportsStatus.value) || "";
    const commune = (els.userReportsCommune && els.userReportsCommune.value.trim().toLowerCase()) || "";

    return reports.filter((r) => {
      if (type && String(r.type || "").toUpperCase() !== type.toUpperCase()) return false;
      if (status && String(r.status || "").toUpperCase() !== status.toUpperCase()) return false;
      if (commune && !String(r.commune || "").toLowerCase().includes(commune)) return false;
      if (!q) return true;
      const haystack = [r.id, r.type, r.status, r.commune, r.description, r.healthStatus, r.petId]
        .filter(Boolean)
        .join(" ")
        .toLowerCase();
      return haystack.includes(q);
    });
  }

  async function deleteUserReport(reportId) {
    if (!Number.isFinite(reportId) || reportId <= 0) return;
    if (
      !window.confirm(
        `¿Eliminar el reporte #${reportId}? Se quitará del mapa y del historial del usuario.`
      )
    ) {
      return;
    }

    try {
      setStatus("Eliminando reporte…", false, true);
      await core.api(`/api/reports/${reportId}`, {
        method: "DELETE",
        token: state.session.token
      });

      state.reports = (state.reports || []).filter((r) => Number(r.id) !== reportId);
      if (state.userModal.reports) {
        state.userModal.reports = state.userModal.reports.filter((r) => Number(r.id) !== reportId);
      }
      if (state.userModal.mediaByReport && state.userModal.mediaByReport[reportId]) {
        delete state.userModal.mediaByReport[reportId];
      }
      state.media = (state.media || []).filter((m) => Number(m.reportId) !== reportId);

      renderUserReportsList();
      renderUsersCards(state.users);
      if (PAGE === "mapa" && state.mapCtrl) {
        renderMap();
      }
      setStatus(`Reporte #${reportId} eliminado.`);
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function renderUserReportsList() {
    if (!els.userReportsList) return;
    const reports = filteredUserReports();

    if (els.userReportsEmpty) {
      els.userReportsEmpty.classList.toggle("hidden", reports.length > 0);
    }

    if (!reports.length) {
      els.userReportsList.innerHTML = "";
      return;
    }

    els.userReportsList.innerHTML = reports
      .map((r) => {
        const photos = mediaForReport(r);
        const thumb = renderReportThumb(r, photos);
        const validPhotos = photos.filter((p) => mediaItemUrl(p));
        const extraPhotos =
          validPhotos.length > 1
            ? `<div class="user-report-item__gallery">${validPhotos
                .slice(1, 4)
                .map(
                  (p) =>
                    `<img src="${core.mediaSrcAttr(mediaItemUrl(p))}" alt="" loading="lazy" decoding="async" class="user-report-item__gallery-img" />`
                )
                .join("")}${validPhotos.length > 4 ? `<span class="user-report-item__more">+${validPhotos.length - 4}</span>` : ""}</div>`
            : "";

        return `
          <article class="user-report-item">
            ${thumb}
            <div class="user-report-item__body">
              <div class="user-report-item__head">
                <strong>#${core.escapeHtml(String(r.id || "-"))}</strong>
                <span class="badge badge-primary">${core.escapeHtml(reportTypeLabel(r.type))}</span>
                <span class="badge">${core.escapeHtml(reportStatusLabel(r.status))}</span>
              </div>
              <p class="user-report-item__meta">
                ${core.escapeHtml(r.commune || "Sin comuna")} · ${core.escapeHtml(core.formatDate(r.createdAt))}
                ${r.petId ? ` · Mascota #${core.escapeHtml(String(r.petId))}` : ""}
              </p>
              <p class="user-report-item__desc">${core.escapeHtml(r.description || "Sin descripción")}</p>
              ${r.healthStatus ? `<p class="user-report-item__health"><i data-lucide="heart-pulse"></i> ${core.escapeHtml(r.healthStatus)}</p>` : ""}
              ${extraPhotos}
              <div class="user-report-item__actions">
                ${
                  r.petId
                    ? `<button type="button" class="btn btn-ghost btn-sm js-view-pet" data-pet-id="${core.escapeHtml(String(r.petId))}"><i data-lucide="paw-print" aria-hidden="true"></i> Ver mascota</button>`
                    : ""
                }
                <button type="button" class="btn btn-secondary btn-sm btn-danger-soft js-delete-report" data-report-id="${core.escapeHtml(String(r.id))}">
                  <i data-lucide="trash-2" aria-hidden="true"></i> Eliminar
                </button>
              </div>
            </div>
          </article>
        `;
      })
      .join("");

    core.refreshIcons();
  }

  async function refreshReportes() {
    return refreshUsuarios();
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
      { label: "Capacidad", value: d.totalCapacityRecords || 0, icon: "users-round", tint: "kpi-ico-sky" },
      { label: "Coincidencias", value: state.matching.length || 0, icon: "sparkles", tint: "kpi-ico-violet" },
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

  function pct(part, total) {
    if (!total) return 0;
    return Math.round((part / total) * 100);
  }

  function riskBucket(level) {
    const l = String(level || "").toUpperCase();
    if (l.includes("ALT") || l === "HIGH") return "ALTO";
    if (l.includes("MED") || l === "MEDIUM") return "MEDIO";
    return "BAJO";
  }

  function reportTypeBucket(type) {
    const t = String(type || "").toUpperCase();
    if (t === "FOUND" || t === "ENCONTRADA") return "HALLAZGO";
    return "PERDIDA";
  }

  function reportStatusBucket(status) {
    const s = String(status || "").toUpperCase();
    if (s === "CLOSED" || s === "CERRADO") return "CERRADO";
    return "ABIERTO";
  }

  function donutHtml(title, segments) {
    const total = segments.reduce((acc, s) => acc + (Number(s.value) || 0), 0);
    let cursor = 0;
    const stops = segments
      .map((s) => {
        const v = Number(s.value) || 0;
        const start = cursor;
        cursor += total ? (v / total) * 100 : 0;
        const end = cursor;
        return `${s.color} ${start.toFixed(2)}% ${end.toFixed(2)}%`;
      })
      .join(", ");

    const centerLabel = total ? `${total}` : "0";
    const centerSub = total ? "items" : "sin datos";

    return `
      <article class="donut-card">
        <div class="donut" style="background: conic-gradient(${stops || "#334155 0% 100%"});">
          <div class="donut__hole">
            <strong>${core.escapeHtml(centerLabel)}</strong>
            <span>${core.escapeHtml(centerSub)}</span>
          </div>
        </div>
        <div class="donut__meta">
          <p class="donut__title">${core.escapeHtml(title)}</p>
          <div class="donut__legend">
            ${segments
              .map((s) => {
                const p = pct(Number(s.value) || 0, total);
                return `<span class="donut__leg"><i style="background:${s.color}"></i>${core.escapeHtml(
                  s.label
                )} <strong>${p}%</strong></span>`;
              })
              .join("")}
          </div>
        </div>
      </article>
    `;
  }

  function renderDonuts() {
    if (!els.adminDonuts) return;

    const reports = Array.isArray(state.reports) ? state.reports : [];
    const zones = Array.isArray(state.zones) ? state.zones : [];

    const typeCounts = reports.reduce(
      (acc, r) => {
        acc[reportTypeBucket(r.type)]++;
        return acc;
      },
      { PERDIDA: 0, HALLAZGO: 0 }
    );

    const statusCounts = reports.reduce(
      (acc, r) => {
        acc[reportStatusBucket(r.status)]++;
        return acc;
      },
      { ABIERTO: 0, CERRADO: 0 }
    );

    const riskCounts = zones.reduce(
      (acc, z) => {
        acc[riskBucket(z.riskLevel)]++;
        return acc;
      },
      { BAJO: 0, MEDIO: 0, ALTO: 0 }
    );

    els.adminDonuts.innerHTML =
      donutHtml("Reportes por tipo", [
        { label: "Perdida", value: typeCounts.PERDIDA, color: "#dc2626" },
        { label: "Hallazgo", value: typeCounts.HALLAZGO, color: "#16a34a" }
      ]) +
      donutHtml("Reportes por estado", [
        { label: "Abierto", value: statusCounts.ABIERTO, color: "#f59e0b" },
        { label: "Cerrado", value: statusCounts.CERRADO, color: "#64748b" }
      ]) +
      donutHtml("Zonas por riesgo", [
        { label: "Bajo", value: riskCounts.BAJO, color: "#16a34a" },
        { label: "Medio", value: riskCounts.MEDIO, color: "#f59e0b" },
        { label: "Alto", value: riskCounts.ALTO, color: "#dc2626" }
      ]);

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
      { name: "Coincidencias IA", path: "/api/matching/health" },
      { name: "Capacidad", path: "/api/capacity/health" },
      { name: "Auditoria", path: "/api/audit/health" },
      { name: "Foro", path: "/api/forum/health" }
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
    const candidates = ["/api/gateway/health", "/api/bff/health", "/actuator/health"];
    const token = core.normalizeToken(state.session.token);

    for (const path of candidates) {
      const controller = new AbortController();
      const timeoutId = setTimeout(() => controller.abort(), 5000);
      try {
        const headers = {};
        if (token) {
          headers.Authorization = `Bearer ${token}`;
        }
        const response = await fetch(`${apiBaseUrl}${path}`, {
          signal: controller.signal,
          headers
        });
        clearTimeout(timeoutId);

        if (!response.ok) {
          continue;
        }

        let status = "UP";
        try {
          const data = await response.json();
          status = String(data.status || "UP").toUpperCase();
        } catch (e) {
          status = "UP";
        }

        return {
          name: "Gateway",
          up: status === "UP",
          detail: status === "UP" ? "Servicio operativo" : `Estado ${status}`
        };
      } catch (error) {
        clearTimeout(timeoutId);
      }
    }

    return { name: "Gateway", up: false, detail: "No responde (revisa URL API y CORS)" };
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
