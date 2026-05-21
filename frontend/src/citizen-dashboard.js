(function () {
  const core = window.SANOS_CORE;
  const MAP_PICK_KEY = "sanos-citizen-map-pick";
  const PAGE = (document.body && document.body.dataset.citizenPage) || "reporte";

  const state = {
    session: core.readSession("citizen"),
    pets: [],
    reports: [],
    media: [],
    matching: [],
    dashboard: null,
    lastReportId: null,
    profileHistoryLoaded: false,
    map: null,
    mapMode: null,
    markerLayer: null,
    pickMarker: null
  };

  const els = {
    citizenIdentity: document.getElementById("citizenIdentity"),
    citizenKpis: document.getElementById("citizenKpis"),
    citizenStatus: document.getElementById("citizenStatus"),
    btnCitizenLogout: document.getElementById("btnCitizenLogout"),
    btnRefresh: document.getElementById("btnRefresh"),
    petForm: document.getElementById("petForm"),
    reportForm: document.getElementById("reportForm"),
    mediaForm: document.getElementById("mediaForm"),
    petsList: document.getElementById("petsList"),
    reportsList: document.getElementById("reportsList"),
    matchingList: document.getElementById("matchingList"),
    reportPetId: document.getElementById("reportPetId"),
    mediaPetId: document.getElementById("mediaPetId"),
    petChip: document.getElementById("petChip"),
    petName: document.getElementById("petName"),
    petSpecies: document.getElementById("petSpecies"),
    petBreed: document.getElementById("petBreed"),
    petColor: document.getElementById("petColor"),
    petSize: document.getElementById("petSize"),
    reportType: document.getElementById("reportType"),
    reportCommune: document.getElementById("reportCommune"),
    reportHealth: document.getElementById("reportHealth"),
    reportLat: document.getElementById("reportLat"),
    reportLng: document.getElementById("reportLng"),
    reportDescription: document.getElementById("reportDescription"),
    mediaFile: document.getElementById("mediaFile"),
    mediaReportId: document.getElementById("mediaReportId"),
    mediaPreview: document.getElementById("mediaPreview"),
    mediaTags: document.getElementById("mediaTags"),
    profileForm: document.getElementById("profileForm"),
    passwordForm: document.getElementById("passwordForm"),
    profileEmail: document.getElementById("profileEmail"),
    profileRut: document.getElementById("profileRut"),
    profileFullName: document.getElementById("profileFullName"),
    profileCommune: document.getElementById("profileCommune"),
    profilePhone: document.getElementById("profilePhone"),
    profileAddress: document.getElementById("profileAddress"),
    profileEmergencyName: document.getElementById("profileEmergencyName"),
    profileEmergencyPhone: document.getElementById("profileEmergencyPhone"),
    pwdCurrent: document.getElementById("pwdCurrent"),
    pwdNew: document.getElementById("pwdNew")
  };

  init();

  function init() {
    if (!state.session.token || !state.session.user) {
      window.location.href = "./index.html";
      return;
    }

    wireCitizenActions();

    if (window.SANOS_DASH_LAYOUT && typeof window.SANOS_DASH_LAYOUT.applyLayout === "function") {
      window.SANOS_DASH_LAYOUT.applyLayout();
    }

    bindPage();

    if (els.citizenIdentity) {
      els.citizenIdentity.textContent = state.session.user.displayName || state.session.user.email;
    }

    wireCitizenNav();
  }

  function wireCitizenActions() {
    document.addEventListener("click", (event) => {
      if (event.target.closest("#btnCitizenLogout")) {
        event.preventDefault();
        onLogout();
        return;
      }
      if (event.target.closest("#btnRefresh")) {
        event.preventDefault();
        refreshForPage();
      }
    });
  }

  function bindPage() {
    const page = PAGE === "resumen" ? "reporte" : PAGE;

    if (page === "reporte" || page === "mascotas" || page === "fotos") {
      if (els.petForm) els.petForm.addEventListener("submit", onCreatePet);
      if (els.reportForm) els.reportForm.addEventListener("submit", onCreateReport);
      if (els.mediaForm) els.mediaForm.addEventListener("submit", onCreateMedia);
      if (els.mediaFile) els.mediaFile.addEventListener("change", onMediaFilePreview);
      applyMapPick();
      syncCoordsHint();
      requestAnimationFrame(() => initReportPickMap());
      refreshReporte();
      return;
    }

    if (page === "mapa") {
      initMap();
      refreshMapa();
      return;
    }

    if (page === "actividad") {
      refreshActividad();
      return;
    }

    if (page === "perfil") {
      if (els.profileForm) els.profileForm.addEventListener("submit", onSaveProfile);
      if (els.passwordForm) els.passwordForm.addEventListener("submit", onChangePassword);
      wireProfileTabs();
      prefillProfileFromSession();
      refreshPerfil();
    }
  }

  function wireProfileTabs() {
    const buttons = document.querySelectorAll("[data-profile-tab]");
    if (!buttons.length) return;

    buttons.forEach((btn) => {
      btn.addEventListener("click", () => {
        switchProfileTab(btn.getAttribute("data-profile-tab"));
      });
    });

    const hash = (window.location.hash || "").replace("#", "");
    if (hash === "historial") {
      switchProfileTab("historial");
    }
  }

  function switchProfileTab(tabKey) {
    const key = tabKey === "historial" ? "historial" : "datos";
    const panelDatos = document.getElementById("profileTabDatos");
    const panelHistorial = document.getElementById("profileTabHistorial");
    const btnDatos = document.getElementById("tabProfileDatos");
    const btnHistorial = document.getElementById("tabProfileHistorial");

    const showHistorial = key === "historial";

    if (panelDatos) {
      panelDatos.classList.toggle("is-active", !showHistorial);
      panelDatos.hidden = showHistorial;
    }
    if (panelHistorial) {
      panelHistorial.classList.toggle("is-active", showHistorial);
      panelHistorial.hidden = !showHistorial;
    }
    if (btnDatos) {
      btnDatos.classList.toggle("is-active", !showHistorial);
      btnDatos.setAttribute("aria-selected", showHistorial ? "false" : "true");
    }
    if (btnHistorial) {
      btnHistorial.classList.toggle("is-active", showHistorial);
      btnHistorial.setAttribute("aria-selected", showHistorial ? "true" : "false");
    }

    if (showHistorial && !state.profileHistoryLoaded) {
      refreshProfileHistory({ quiet: false });
    }

    core.refreshIcons();
  }

  function profileFields() {
    return {
      profileEmail: document.getElementById("profileEmail"),
      profileRut: document.getElementById("profileRut"),
      profileFullName: document.getElementById("profileFullName"),
      profileCommune: document.getElementById("profileCommune"),
      profilePhone: document.getElementById("profilePhone"),
      profileAddress: document.getElementById("profileAddress"),
      profileEmergencyName: document.getElementById("profileEmergencyName"),
      profileEmergencyPhone: document.getElementById("profileEmergencyPhone")
    };
  }

  function normalizeProfile(raw) {
    if (!raw || typeof raw !== "object") return null;
    return {
      id: raw.id,
      email: raw.email || "",
      displayName: raw.displayName || "",
      fullName: raw.fullName || raw.full_name || "",
      rut: raw.rut || raw.rutDocument || raw.rut_document || "",
      commune: raw.commune || raw.comuna || "",
      address: raw.address || raw.direccion || "",
      phone: raw.phone || raw.telefonoPrincipal || raw.telefono_principal || "",
      emergencyContactName:
        raw.emergencyContactName || raw.emergency_contact_name || raw.contactoEmergenciaNombre || "",
      emergencyContactPhone:
        raw.emergencyContactPhone || raw.emergency_contact_phone || raw.contactoEmergenciaTelefono || "",
      role: raw.role,
      createdAt: raw.createdAt || raw.created_at
    };
  }

  function prefillProfileFromSession() {
    const user = state.session.user || {};
    const fields = profileFields();
    if (fields.profileEmail) fields.profileEmail.value = user.email || "";
    if (fields.profileFullName) {
      fields.profileFullName.value = user.fullName || user.displayName || "";
    }
    if (fields.profileCommune && user.commune) fields.profileCommune.value = user.commune;
    if (fields.profilePhone && user.phone) fields.profilePhone.value = user.phone;
    if (fields.profileAddress && user.address) fields.profileAddress.value = user.address;
  }

  async function loadCitizenProfile() {
    state.session = core.readSession("citizen");
    const token = state.session.token;
    if (!token) {
      throw new Error("Sesion expirada. Vuelve a iniciar sesion.");
    }
    const opts = { token };
    const uid = userId();
    const paths = ["/api/iam/profile", "/api/iam/users/me"];
    if (uid) paths.push(`/api/iam/users/${uid}`);

    let lastError = null;
    for (const path of paths) {
      try {
        const data = await core.api(path, opts);
        return normalizeProfile(data);
      } catch (error) {
        lastError = error;
      }
    }
    throw lastError || new Error("No se pudo cargar el perfil");
  }

  function userId() {
    const id = state.session.user && state.session.user.id;
    const n = Number(id);
    return Number.isFinite(n) && n > 0 ? n : null;
  }

  function wireCitizenNav() {
    const cur = (window.location.pathname.split("/").pop() || "").toLowerCase();
    const alias =
      cur === "citizen-fotos.html" ||
      cur === "citizen-mascotas.html" ||
      cur === "citizen-resumen.html"
        ? "citizen-reporte.html"
        : cur;
    document.querySelectorAll(".dash-nav__link[href], .dash-sidebar__link[href]").forEach((a) => {
      const href = (a.getAttribute("href") || "").toLowerCase();
      const match =
        href === alias ||
        (alias === "citizen-dashboard.html" && href === "citizen-reporte.html");
      a.classList.toggle("is-active", match);
      if (match) {
        a.setAttribute("aria-current", "page");
      } else {
        a.removeAttribute("aria-current");
      }
    });
  }

  function refreshForPage() {
    const page = PAGE === "resumen" ? "reporte" : PAGE;
    if (page === "reporte" || page === "mascotas" || page === "fotos") return refreshReporte();
    if (page === "mapa") return refreshMapa();
    if (page === "actividad") return refreshActividad();
    if (page === "perfil") return refreshPerfil();
  }

  function saveMapPick(lat, lng) {
    try {
      window.localStorage.setItem(
        MAP_PICK_KEY,
        JSON.stringify({ lat: Number(lat.toFixed(6)), lng: Number(lng.toFixed(6)), savedAt: Date.now() })
      );
    } catch (e) {
      /* ignore */
    }
  }

  function applyMapPick() {
    if (!els.reportLat || !els.reportLng) return;
    try {
      const raw = window.localStorage.getItem(MAP_PICK_KEY);
      if (!raw) return;
      const p = JSON.parse(raw);
      if (!p || typeof p.lat !== "number" || typeof p.lng !== "number") return;
      if (Date.now() - (p.savedAt || 0) > 86400000) {
        window.localStorage.removeItem(MAP_PICK_KEY);
        return;
      }
      setReportCoords(p.lat, p.lng, false);
    } catch (e) {
      /* ignore */
    }
  }

  function syncCoordsHint() {
    const hint = document.getElementById("reportCoordsHint");
    if (!hint || !els.reportLat || !els.reportLng) return;
    const lat = Number(els.reportLat.value);
    const lng = Number(els.reportLng.value);
    if (Number.isFinite(lat) && Number.isFinite(lng)) {
      hint.textContent = `Ubicación: ${lat.toFixed(5)}, ${lng.toFixed(5)}`;
      hint.classList.add("is-set");
    } else {
      hint.textContent = "Sin ubicación seleccionada — haz clic en el mapa";
      hint.classList.remove("is-set");
    }
  }

  function setReportCoords(lat, lng, panMap) {
    if (!els.reportLat || !els.reportLng) return;
    const latN = Number(lat);
    const lngN = Number(lng);
    if (Number.isNaN(latN) || Number.isNaN(lngN)) return;
    els.reportLat.value = latN.toFixed(6);
    els.reportLng.value = lngN.toFixed(6);
    saveMapPick(latN, lngN);
    syncCoordsHint();
    showPickMarker(latN, lngN, panMap !== false);
  }

  function showPickMarker(lat, lng, panMap) {
    if (!state.map || typeof L === "undefined") return;
    if (state.pickMarker) {
      state.pickMarker.setLatLng([lat, lng]);
    } else {
      state.pickMarker = L.circleMarker([lat, lng], {
        radius: 10,
        color: "#f59e0b",
        fillColor: "#fbbf24",
        fillOpacity: 0.9,
        weight: 2
      }).addTo(state.map);
    }
    state.pickMarker
      .bindPopup(
        `<strong>Ubicación del reporte</strong><br/>Lat ${lat.toFixed(5)}, Lng ${lng.toFixed(5)}`
      )
      .openPopup();
    if (panMap) {
      state.map.setView([lat, lng], Math.max(state.map.getZoom(), 14));
    }
  }

  function restorePickMarkerFromInputs() {
    if (!els.reportLat || !els.reportLng) return;
    const lat = Number(els.reportLat.value);
    const lng = Number(els.reportLng.value);
    if (Number.isFinite(lat) && Number.isFinite(lng)) {
      showPickMarker(lat, lng, false);
    }
  }

  function initReportPickMap() {
    if (typeof L === "undefined") return;
    const container = document.getElementById("reportPickMap");
    if (!container) return;

    if (state.map && state.mapMode === "report-pick") {
      requestAnimationFrame(() => state.map.invalidateSize());
      restorePickMarkerFromInputs();
      return;
    }

    if (state.map) {
      state.map.remove();
      state.map = null;
      state.markerLayer = null;
      state.pickMarker = null;
    }

    const settings = core.loadSettings();
    const center = settings.defaultCenter || { lat: -33.4489, lng: -70.6693 };
    let startLat = center.lat;
    let startLng = center.lng;
    if (els.reportLat && els.reportLng && els.reportLat.value && els.reportLng.value) {
      const lat = Number(els.reportLat.value);
      const lng = Number(els.reportLng.value);
      if (Number.isFinite(lat) && Number.isFinite(lng)) {
        startLat = lat;
        startLng = lng;
      }
    }

    state.map = L.map(container).setView([startLat, startLng], 13);
    state.mapMode = "report-pick";
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap"
    }).addTo(state.map);
    state.markerLayer = L.layerGroup().addTo(state.map);
    state.pickMarker = null;

    state.map.on("click", (event) => {
      const { lat, lng } = event.latlng;
      setReportCoords(lat, lng, false);
    });

    requestAnimationFrame(() => {
      state.map.invalidateSize();
      restorePickMarkerFromInputs();
    });
  }

  function initMap() {
    if (typeof L === "undefined") return;
    const container = document.getElementById("citizenMap");
    if (!container) return;

    if (state.map && state.mapMode === "community") {
      requestAnimationFrame(() => state.map.invalidateSize());
      return;
    }

    if (state.map) {
      state.map.remove();
      state.map = null;
      state.markerLayer = null;
      state.pickMarker = null;
    }

    const settings = core.loadSettings();
    const center = settings.defaultCenter || { lat: -33.4489, lng: -70.6693 };
    state.map = L.map(container).setView([center.lat, center.lng], 12);
    state.mapMode = "community";
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap"
    }).addTo(state.map);
    state.markerLayer = L.layerGroup().addTo(state.map);

    state.map.on("click", (event) => {
      const { lat, lng } = event.latlng;
      saveMapPick(lat, lng);
      setStatus(`Coordenadas guardadas (${lat.toFixed(5)}, ${lng.toFixed(5)}). Úsalas en Hacer reporte.`);
      if (state.pickMarker) {
        state.pickMarker.setLatLng([lat, lng]);
      } else {
        state.pickMarker = L.circleMarker([lat, lng], {
          radius: 9,
          color: "#f59e0b",
          fillColor: "#fbbf24",
          fillOpacity: 0.85
        }).addTo(state.map);
      }
      state.pickMarker
        .bindPopup(
          "Coordenadas guardadas.<br/>" +
            `Lat ${lat.toFixed(5)}, Lng ${lng.toFixed(5)}<br/>` +
            "<small>Se aplican al publicar en <strong>Hacer reporte</strong>.</small>"
        )
        .openPopup();
    });
  }

  function renderReportsOnMap() {
    if (!state.map || !state.markerLayer) return;
    state.markerLayer.clearLayers();
    const bounds = [];
    state.reports.forEach((report) => {
      if (report.latitude == null || report.longitude == null) return;
      const lat = Number(report.latitude);
      const lng = Number(report.longitude);
      if (Number.isNaN(lat) || Number.isNaN(lng)) return;
      const isLost = String(report.type || "").toUpperCase() === "LOST";
      const color = isLost ? "#c0392b" : "#3d8f73";
      const marker = L.circleMarker([lat, lng], {
        radius: 8,
        color,
        fillColor: color,
        fillOpacity: 0.75
      });
      marker.bindPopup(
        `<strong>${core.escapeHtml(report.type || "Reporte")}</strong><br/>` +
          `${core.escapeHtml(report.commune || "Sin comuna")}<br/>` +
          `<em>${core.escapeHtml(report.description || "Sin descripcion")}</em>`
      );
      marker.addTo(state.markerLayer);
      bounds.push([lat, lng]);
    });
    if (bounds.length && state.mapMode !== "report-pick") {
      state.map.fitBounds(bounds, { padding: [30, 30], maxZoom: 14 });
    }
  }

  async function refreshResumen() {
    try {
      setStatus("Sincronizando…", false, true);
      const [dashboard, pets, reports, media, matching] = await Promise.all([
        core.api("/api/bff/dashboard", { token: state.session.token }),
        core.api("/api/pets", { token: state.session.token }),
        core.api("/api/reports", { token: state.session.token }),
        core.api("/api/media", { token: state.session.token }),
        core.api("/api/matching", { token: state.session.token })
      ]);
      state.dashboard = dashboard;
      state.pets = pets;
      state.reports = reports;
      state.media = media;
      state.matching = matching;
      renderKpis();
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshMapa() {
    try {
      setStatus("Sincronizando…", false, true);
      state.reports = await core.api("/api/reports", { token: state.session.token });
      renderReportsOnMap();
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshReporte() {
    return refreshMascotas();
  }

  async function refreshMascotas() {
    try {
      setStatus("Sincronizando…", false, true);
      const uid = userId();
      const petsPath = uid ? `/api/pets/owner/${uid}` : "/api/pets";
      const reportsPath = uid ? `/api/reports/user/${uid}` : "/api/reports";
      const [pets, reports, dashboard] = await Promise.all([
        core.api(petsPath, { token: state.session.token }),
        core.api(reportsPath, { token: state.session.token }),
        core.api("/api/bff/dashboard", { token: state.session.token }).catch(() => ({}))
      ]);
      state.pets = pets;
      state.reports = reports;
      state.dashboard = dashboard;
      renderPetOptions();
      renderReportOptions();
      if (PAGE === "reporte" || PAGE === "mascotas" || PAGE === "fotos") {
        initReportPickMap();
        renderReportsOnMap();
        restorePickMarkerFromInputs();
      }
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshPerfil() {
    try {
      setStatus("Cargando perfil…", false, true);
      state.profileHistoryLoaded = false;
      const [profile] = await Promise.all([
        loadCitizenProfile(),
        refreshProfileHistory({ quiet: true }).catch(() => null)
      ]);
      fillProfileForm(profile);
      mergeSessionUser(profile);
      setStatus("Perfil cargado.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshProfileHistory(options) {
    const quiet = options && options.quiet;
    const uid = userId();
    if (!uid) {
      renderProfileHistory([], []);
      return;
    }

    try {
      if (!quiet) setStatus("Cargando historial…", false, true);
      const [reports, pets] = await Promise.all([
        core.api(`/api/reports/user/${uid}`, { token: state.session.token }),
        core.api(`/api/pets/owner/${uid}`, { token: state.session.token }).catch(() => [])
      ]);
      state.reports = reports;
      state.pets = pets;
      state.profileHistoryLoaded = true;
      renderProfileHistory(reports, pets);
      if (!quiet) setStatus("Historial actualizado.");
    } catch (error) {
      renderProfileHistory([], state.pets || []);
      if (!quiet) setStatus(error.message, true);
      throw error;
    }
  }

  function petNameById(petId, pets) {
    const list = pets || state.pets || [];
    const pet = list.find((p) => String(p.id) === String(petId));
    if (!pet) return petId ? `#${petId}` : "—";
    return pet.name ? `${pet.name}` : `#${pet.id}`;
  }

  function formatReportType(type) {
    const t = String(type || "").toUpperCase();
    if (t === "LOST" || t === "PERDIDA") return "Perdida";
    if (t === "FOUND" || t === "ENCONTRADA") return "Encontrada";
    return type || "—";
  }

  function formatReportStatus(status) {
    const s = String(status || "").toUpperCase();
    if (s === "OPEN" || s === "ABIERTO") return "Abierto";
    if (s === "CLOSED" || s === "CERRADO") return "Cerrado";
    if (s === "RESOLVED" || s === "RESUELTO") return "Resuelto";
    return status || "—";
  }

  function reportTypeBadge(type) {
    const t = String(type || "").toUpperCase();
    const label = formatReportType(type);
    const cls =
      t === "LOST" || t === "PERDIDA"
        ? "report-badge--lost"
        : t === "FOUND" || t === "ENCONTRADA"
          ? "report-badge--found"
          : "report-badge--default";
    return `<span class="report-badge ${cls}">${core.escapeHtml(label)}</span>`;
  }

  function reportStatusBadge(status) {
    const s = String(status || "").toUpperCase();
    const label = formatReportStatus(status);
    let cls = "report-badge--default";
    if (s === "OPEN" || s === "ABIERTO") cls = "report-badge--open";
    else if (s === "CLOSED" || s === "CERRADO" || s === "RESOLVED" || s === "RESUELTO") {
      cls = "report-badge--closed";
    }
    return `<span class="report-badge ${cls}">${core.escapeHtml(label)}</span>`;
  }

  function renderProfileHistory(reports, pets) {
    const tbody = document.getElementById("profileHistoryTable");
    const empty = document.getElementById("profileHistoryEmpty");
    const summary = document.getElementById("profileHistorySummary");
    const tableWrap = document.querySelector(".profile-history-table");

    const list = Array.isArray(reports) ? reports : state.reports || [];
    const petList = Array.isArray(pets) ? pets : state.pets || [];

    if (summary) {
      const lost = list.filter((r) => String(r.type || "").toUpperCase() === "LOST").length;
      const found = list.filter((r) => String(r.type || "").toUpperCase() === "FOUND").length;
      const open = list.filter((r) => {
        const s = String(r.status || "").toUpperCase();
        return s === "OPEN" || s === "ABIERTO";
      }).length;
      summary.innerHTML = `
        <div class="profile-history-stat"><span>Total reportes</span><strong>${list.length}</strong></div>
        <div class="profile-history-stat"><span>Perdidas</span><strong>${lost}</strong></div>
        <div class="profile-history-stat"><span>Encontradas</span><strong>${found}</strong></div>
        <div class="profile-history-stat"><span>Abiertos</span><strong>${open}</strong></div>
      `;
    }

    if (!tbody) return;

    if (!list.length) {
      tbody.innerHTML = "";
      if (empty) empty.classList.remove("hidden");
      if (tableWrap) tableWrap.classList.add("hidden");
      return;
    }

    if (empty) empty.classList.add("hidden");
    if (tableWrap) tableWrap.classList.remove("hidden");

    const sorted = [...list].sort((a, b) => {
      const da = new Date(a.createdAt || 0).getTime();
      const db = new Date(b.createdAt || 0).getTime();
      return db - da;
    });

    tbody.innerHTML = sorted
      .map((report) => {
        const petLabel = petNameById(report.petId, petList);
        return `<tr>
          <td>${core.escapeHtml(String(report.id || "—"))}</td>
          <td>${reportTypeBadge(report.type)}</td>
          <td>${reportStatusBadge(report.status)}</td>
          <td>${core.escapeHtml(petLabel)}</td>
          <td>${core.escapeHtml(report.commune || "—")}</td>
          <td>${core.escapeHtml(report.healthStatus || "—")}</td>
          <td>${core.escapeHtml(core.formatDate(report.createdAt))}</td>
          <td>${core.escapeHtml(core.truncate(report.description || "—", 80))}</td>
        </tr>`;
      })
      .join("");
  }

  function fillProfileForm(profile) {
    if (!profile) return;
    const fields = profileFields();
    if (fields.profileEmail) fields.profileEmail.value = profile.email || "";
    if (fields.profileRut) fields.profileRut.value = profile.rut || "";
    if (fields.profileFullName) {
      fields.profileFullName.value = profile.fullName || profile.displayName || "";
    }
    if (fields.profileCommune) fields.profileCommune.value = profile.commune || "";
    if (fields.profilePhone) fields.profilePhone.value = profile.phone || "";
    if (fields.profileAddress) fields.profileAddress.value = profile.address || "";
    if (fields.profileEmergencyName) {
      fields.profileEmergencyName.value = profile.emergencyContactName || "";
    }
    if (fields.profileEmergencyPhone) {
      fields.profileEmergencyPhone.value = profile.emergencyContactPhone || "";
    }
  }

  function mergeSessionUser(profile) {
    const p = normalizeProfile(profile) || profile;
    if (!p || !state.session.user) return;
    state.session.user = {
      ...state.session.user,
      id: p.id,
      email: p.email,
      displayName: p.displayName,
      fullName: p.fullName,
      rut: p.rut,
      commune: p.commune,
      phone: p.phone,
      address: p.address,
      emergencyContactName: p.emergencyContactName,
      emergencyContactPhone: p.emergencyContactPhone
    };
    core.writeSession("citizen", state.session);
    const identity = document.getElementById("citizenIdentity");
    if (identity) {
      identity.textContent = state.session.user.displayName || state.session.user.email;
    }
  }

  async function onSaveProfile(event) {
    event.preventDefault();
    if (!els.profileFullName) return;
    const body = {
      fullName: els.profileFullName.value.trim(),
      commune: els.profileCommune ? els.profileCommune.value.trim() : "",
      address: els.profileAddress ? els.profileAddress.value.trim() : "",
      phone: els.profilePhone ? els.profilePhone.value.trim() : "",
      emergencyContactName: els.profileEmergencyName ? els.profileEmergencyName.value.trim() : "",
      emergencyContactPhone: els.profileEmergencyPhone ? els.profileEmergencyPhone.value.trim() : ""
    };
    if (!body.fullName) {
      return setStatus("El nombre completo es obligatorio.", true);
    }
    try {
      const updated = await core.api("/api/iam/profile", {
        method: "PATCH",
        token: state.session.token,
        body
      });
      fillProfileForm(normalizeProfile(updated));
      mergeSessionUser(updated);
      setStatus("Perfil actualizado.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function onChangePassword(event) {
    event.preventDefault();
    if (!els.pwdCurrent || !els.pwdNew) return;
    const currentPassword = els.pwdCurrent.value;
    const newPassword = els.pwdNew.value;
    if (!currentPassword || !newPassword) {
      return setStatus("Completa ambas contraseñas.", true);
    }
    try {
      await core.api("/api/iam/change-password", {
        method: "POST",
        token: state.session.token,
        body: { currentPassword, newPassword }
      });
      els.passwordForm.reset();
      setStatus("Contraseña actualizada.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshFotos() {
    try {
      setStatus("Sincronizando…", false, true);
      state.pets = await core.api("/api/pets", { token: state.session.token });
      renderPetOptionsMediaOnly();
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function refreshActividad() {
    try {
      setStatus("Sincronizando…", false, true);
      const uid = userId();
      const petsPath = uid ? `/api/pets/owner/${uid}` : "/api/pets";
      const reportsPath = uid ? `/api/reports/user/${uid}` : "/api/reports";
      const [pets, reports, matching] = await Promise.all([
        core.api(petsPath, { token: state.session.token }),
        core.api(reportsPath, { token: state.session.token }),
        core.api("/api/matching", { token: state.session.token }).catch(() => [])
      ]);
      state.pets = pets;
      state.reports = reports;
      state.matching = matching;
      renderLists();
      setStatus("Listo");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function onCreatePet(event) {
    event.preventDefault();
    if (!els.petChip || !els.petName) return;

    const payload = {
      chipNumber: els.petChip.value.trim(),
      name: els.petName.value.trim(),
      species: els.petSpecies.value,
      breed: els.petBreed.value.trim(),
      color: els.petColor.value.trim(),
      size: els.petSize.value.trim(),
      ownerId: userId()
    };

    if (!payload.chipNumber || !payload.name) {
      return setStatus("Mascota requiere chip y nombre.", true);
    }
    if (!payload.ownerId) {
      return setStatus("Sesion invalida: vuelve a iniciar sesion.", true);
    }

    try {
      await core.api("/api/pets", { method: "POST", token: state.session.token, body: payload });
      els.petForm.reset();
      els.petSpecies.value = "DOG";
      await refreshMascotas();
      setStatus("Mascota registrada.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function onCreateReport(event) {
    event.preventDefault();
    if (!els.reportPetId) return;

    const payload = {
      petId: els.reportPetId.value,
      type: els.reportType.value,
      status: "OPEN",
      healthStatus: els.reportHealth.value.trim(),
      commune: els.reportCommune.value.trim(),
      latitude: Number(els.reportLat.value),
      longitude: Number(els.reportLng.value),
      description: els.reportDescription.value.trim(),
      createdBy: userId()
    };

    if (!payload.createdBy) {
      return setStatus("Sesion invalida: vuelve a iniciar sesion.", true);
    }
    if (!payload.petId || Number.isNaN(payload.latitude) || Number.isNaN(payload.longitude)) {
      return setStatus("Reporte requiere mascota, latitud y longitud validas.", true);
    }

    try {
      const created = await core.api("/api/reports", {
        method: "POST",
        token: state.session.token,
        body: payload
      });
      state.lastReportId = created && created.id ? created.id : null;
      els.reportForm.reset();
      try {
        window.localStorage.removeItem(MAP_PICK_KEY);
      } catch (e) {
        /* ignore */
      }
      if (state.pickMarker && state.map) {
        state.map.removeLayer(state.pickMarker);
        state.pickMarker = null;
      }
      syncCoordsHint();
      await refreshMascotas();
      if (els.mediaReportId && state.lastReportId) {
        els.mediaReportId.value = String(state.lastReportId);
      }
      setStatus("Reporte guardado en la base de datos.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function onMediaFilePreview() {
    if (!els.mediaFile || !els.mediaPreview) return;
    const file = els.mediaFile.files && els.mediaFile.files[0];
    if (!file) {
      els.mediaPreview.classList.add("hidden");
      els.mediaPreview.innerHTML = "";
      return;
    }
    const url = URL.createObjectURL(file);
    els.mediaPreview.classList.remove("hidden");
    els.mediaPreview.innerHTML = `<img src="${url}" alt="Vista previa" style="max-width:100%;border-radius:12px;max-height:220px;" />`;
  }

  async function onCreateMedia(event) {
    event.preventDefault();
    if (!els.mediaPetId || !els.mediaFile) return;

    const file = els.mediaFile.files && els.mediaFile.files[0];
    const petId = els.mediaPetId.value;
    const reportId = els.mediaReportId ? els.mediaReportId.value : "";
    const tags = els.mediaTags.value.trim();

    if (!petId || !file) {
      return setStatus("Selecciona mascota y una foto.", true);
    }

    const formData = new FormData();
    formData.append("file", file);
    formData.append("petId", petId);
    if (reportId) formData.append("reportId", reportId);
    if (tags) formData.append("tags", tags);

    try {
      const uploaded = await core.apiUpload("/api/media/upload", formData, {
        token: state.session.token
      });
      els.mediaForm.reset();
      if (els.mediaPreview) {
        els.mediaPreview.classList.add("hidden");
        els.mediaPreview.innerHTML = "";
      }
      if (uploaded && uploaded.url && els.mediaPreview) {
        const src = core.mediaUrl(uploaded.url).replace(/"/g, "&quot;");
        els.mediaPreview.classList.remove("hidden");
        els.mediaPreview.innerHTML = `<img src="${src}" alt="Foto subida" style="max-width:100%;border-radius:12px;max-height:220px;" />`;
      }
      await refreshReporte();
      setStatus("Foto guardada en la base de datos.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function onLogout() {
    core.clearSession("citizen");
    window.location.href = "./index.html?logout=1";
  }

  function renderKpis() {
    if (!els.citizenKpis) return;
    const d = state.dashboard || {};
    const cards = [
      { label: "Mascotas", value: d.totalPets || state.pets.length || 0, icon: "paw-print", tint: "kpi-ico-indigo" },
      { label: "Reportes", value: d.totalReports || state.reports.length || 0, icon: "file-text", tint: "kpi-ico-mint" },
      { label: "IA", value: state.matching.length || 0, icon: "sparkles", tint: "kpi-ico-gold" },
      { label: "Fotos", value: d.totalPhotos || state.media.length || 0, icon: "image", tint: "kpi-ico-sky" }
    ];

    els.citizenKpis.innerHTML = cards
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

  function renderPetOptions() {
    if (!els.reportPetId) return;
    const options = state.pets.length
      ? state.pets
          .map((pet) => {
            const label = `${pet.name || "Mascota"} - ${pet.chipNumber || "Sin chip"}`;
            return `<option value="${core.escapeHtml(pet.id)}">${core.escapeHtml(label)}</option>`;
          })
          .join("")
      : '<option value="">Primero registra una mascota</option>';

    els.reportPetId.innerHTML = options;
    if (els.mediaPetId) els.mediaPetId.innerHTML = options;
    renderReportOptions();
  }

  function renderReportOptions() {
    if (!els.mediaReportId) return;
    const base = '<option value="">Sin vincular</option>';
    const reportOpts = state.reports.length
      ? state.reports
          .map((r) => {
            const label = `${r.type || "Reporte"} #${r.id || "?"} - ${r.commune || "sin comuna"}`;
            const selected =
              state.lastReportId && String(r.id) === String(state.lastReportId) ? " selected" : "";
            return `<option value="${core.escapeHtml(r.id)}"${selected}>${core.escapeHtml(label)}</option>`;
          })
          .join("")
      : "";
    els.mediaReportId.innerHTML = base + reportOpts;
  }

  function renderPetOptionsMediaOnly() {
    if (!els.mediaPetId) return;
    const options = state.pets.length
      ? state.pets
          .map((pet) => {
            const label = `${pet.name || "Mascota"} - ${pet.chipNumber || "Sin chip"}`;
            return `<option value="${core.escapeHtml(pet.id)}">${core.escapeHtml(label)}</option>`;
          })
          .join("")
      : '<option value="">Primero registra una mascota</option>';
    els.mediaPetId.innerHTML = options;
  }

  function renderLists() {
    if (!els.petsList || !els.reportsList || !els.matchingList) return;
    els.petsList.innerHTML = renderStack(
      state.pets.slice(0, 5).map((pet) => ({
        title: pet.name || "Mascota sin nombre",
        meta: `${pet.species || "N/A"} - ${pet.breed || "Sin raza"}`
      })),
      "Sin mascotas registradas aun."
    );

    els.reportsList.innerHTML = renderStack(
      state.reports.slice(0, 8).map((report) => ({
        title: `${report.type || "REPORTE"} #${report.id || "-"} - ${report.status || ""}`,
        meta: `${report.commune || "Sin comuna"} · ${core.truncate(report.description || "Sin descripcion", 70)}`
      })),
      "Aun no has publicado reportes."
    );

    els.matchingList.innerHTML = renderStack(
      state.matching.slice(0, 5).map((match) => ({
        title: `Match ${match.id || "-"} - Score ${match.score || "0"}`,
        meta: core.truncate(match.explanation || "Sin explicacion de coincidencia", 90)
      })),
      "Sin coincidencias IA registradas aun."
    );
    core.refreshIcons();
  }

  function renderStack(items, emptyMessage) {
    if (!items.length) {
      return `<article class="stack-item"><strong>Sin datos</strong><p>${core.escapeHtml(emptyMessage)}</p></article>`;
    }

    return items
      .map(
        (item) => `
          <article class="stack-item">
            <strong>${core.escapeHtml(item.title)}</strong>
            <p>${core.escapeHtml(item.meta)}</p>
          </article>
        `
      )
      .join("");
  }

  function statusNode() {
    return document.querySelector(".dash-topbar #citizenStatus") || document.getElementById("citizenStatus");
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
