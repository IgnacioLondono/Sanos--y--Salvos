(function () {
  const defaults = window.SANOS_CONFIG || {};

  const storageKeys = {
    settings: "sanos-settings",
    citizenSession: "sanos-session",
    adminSession: "sanos-admin-session"
  };

  function inferApiBaseUrl() {
    const { protocol, hostname } = window.location;
    return `${protocol}//${hostname}:8080`;
  }

  function sanitizeBaseUrl(value) {
    return String(value || "").trim().replace(/\/$/, "");
  }

  function loadSettings() {
    const fallback = {
      apiBaseUrl: defaults.apiBaseUrl || inferApiBaseUrl(),
      mapsApiKey: defaults.googleMapsApiKey || "",
      defaultCenter: defaults.defaultCenter || { lat: -33.4489, lng: -70.6693 }
    };

    const raw = window.localStorage.getItem(storageKeys.settings);
    if (!raw) {
      return fallback;
    }

    try {
      const data = JSON.parse(raw);
      return {
        apiBaseUrl: sanitizeBaseUrl(data.apiBaseUrl) || fallback.apiBaseUrl,
        mapsApiKey: data.mapsApiKey || fallback.mapsApiKey,
        defaultCenter: data.defaultCenter || fallback.defaultCenter
      };
    } catch (error) {
      window.localStorage.removeItem(storageKeys.settings);
      return fallback;
    }
  }

  function saveSettings(settings) {
    const payload = {
      apiBaseUrl: sanitizeBaseUrl(settings.apiBaseUrl) || inferApiBaseUrl(),
      mapsApiKey: settings.mapsApiKey || "",
      defaultCenter: settings.defaultCenter || { lat: -33.4489, lng: -70.6693 }
    };
    window.localStorage.setItem(storageKeys.settings, JSON.stringify(payload));
    return payload;
  }

  function readSession(type) {
    const key = type === "admin" ? storageKeys.adminSession : storageKeys.citizenSession;
    const raw = window.localStorage.getItem(key);
    if (!raw) {
      return { token: "", user: null };
    }

    try {
      const data = JSON.parse(raw);
      return {
        token: data.token || "",
        user: data.user || null
      };
    } catch (error) {
      window.localStorage.removeItem(key);
      return { token: "", user: null };
    }
  }

  function writeSession(type, session) {
    const key = type === "admin" ? storageKeys.adminSession : storageKeys.citizenSession;
    window.localStorage.setItem(
      key,
      JSON.stringify({ token: session.token || "", user: session.user || null })
    );
  }

  function clearSession(type) {
    const key = type === "admin" ? storageKeys.adminSession : storageKeys.citizenSession;
    window.localStorage.removeItem(key);
  }

  function normalizeToken(token) {
    if (!token) return "";
    let value = String(token).trim();
    if (/^bearer\s+/i.test(value)) {
      value = value.replace(/^bearer\s+/i, "").trim();
    }
    return value;
  }

  function apiErrorMessage(status, path, data) {
    const p = String(path || "");
    if (status === 401) {
      if (p.includes("/api/iam/login")) {
        return "Datos incorrectos, revise correo u contraseña";
      }
      return "Sesión expirada o no válida. Cierra sesión e inicia de nuevo.";
    }
    return (
      (data && data.error) ||
      (typeof data === "string" ? data : "") ||
      `Error ${status} en ${path}`
    );
  }

  function isUnauthorizedError(error) {
    return Boolean(error && (error.status === 401 || /401/.test(String(error.message || ""))));
  }

  function redirectToLogin(reason) {
    const base = indexUrl();
    const q = reason ? `?logout=1&reason=${encodeURIComponent(reason)}` : "?logout=1";
    window.location.href = `${base}${q}`;
  }

  async function api(path, options) {
    const opts = options || {};
    const settings = loadSettings();
    const method = opts.method || "GET";
    const headers = { "Content-Type": "application/json" };
    const token = normalizeToken(opts.token);

    if (opts.auth !== false && token) {
      headers.Authorization = `Bearer ${token}`;
    }

    let response;
    try {
      response = await fetch(`${settings.apiBaseUrl}${path}`, {
        method,
        headers,
        body: opts.body ? JSON.stringify(opts.body) : undefined
      });
    } catch (error) {
      throw new Error(
        `No se pudo conectar con el backend (${settings.apiBaseUrl}). Verifica gateway encendido, URL API y CORS.`
      );
    }

    const text = await response.text();
    let data = null;

    if (text) {
      try {
        data = JSON.parse(text);
      } catch (error) {
        data = text;
      }
    }

    if (!response.ok) {
      const message = apiErrorMessage(response.status, path, data);
      const error = new Error(message);
      error.status = response.status;
      error.path = path;
      throw error;
    }

    return data;
  }

  function getApiBaseUrl() {
    return loadSettings().apiBaseUrl;
  }

  function mediaUrl(pathOrUrl) {
    if (!pathOrUrl) return "";
    let value = String(pathOrUrl).trim();
    if (!value) return "";

    if (value.startsWith("http://") || value.startsWith("https://")) {
      return value;
    }

    const base = getApiBaseUrl();
    if (value.startsWith("/")) {
      return `${base}${value}`;
    }
    if (value.startsWith("api/")) {
      return `${base}/${value}`;
    }
    return `${base}/${value}`;
  }

  function mediaSrcAttr(pathOrUrl) {
    const resolved = mediaUrl(pathOrUrl);
    if (!resolved) return "";
    return resolved.replace(/"/g, "%22").replace(/'/g, "%27");
  }

  async function apiUpload(path, formData, options) {
    const opts = options || {};
    const settings = loadSettings();
    const headers = {};

    const token = normalizeToken(opts.token);

    if (opts.auth !== false && token) {
      headers.Authorization = `Bearer ${token}`;
    }

    let response;
    try {
      response = await fetch(`${settings.apiBaseUrl}${path}`, {
        method: "POST",
        headers,
        body: formData
      });
    } catch (error) {
      throw new Error(
        `No se pudo conectar con el backend (${settings.apiBaseUrl}). Verifica gateway encendido, URL API y CORS.`
      );
    }

    const text = await response.text();
    let data = null;

    if (text) {
      try {
        data = JSON.parse(text);
      } catch (error) {
        data = text;
      }
    }

    if (!response.ok) {
      const message = apiErrorMessage(response.status, path, data);
      const error = new Error(message);
      error.status = response.status;
      error.path = path;
      throw error;
    }

    return data;
  }

  function escapeHtml(value) {
    return String(value)
      .replaceAll("&", "&amp;")
      .replaceAll("<", "&lt;")
      .replaceAll(">", "&gt;")
      .replaceAll('"', "&quot;")
      .replaceAll("'", "&#39;");
  }

  function truncate(value, size) {
    const text = String(value || "");
    return text.length > size ? `${text.slice(0, size - 1)}...` : text;
  }

  function formatDate(value) {
    if (!value) {
      return "-";
    }

    const date = new Date(value);
    if (Number.isNaN(date.getTime())) {
      return String(value);
    }

    return date.toLocaleString("es-CL", {
      day: "2-digit",
      month: "2-digit",
      year: "numeric",
      hour: "2-digit",
      minute: "2-digit",
      hour12: false
    });
  }

  /** Fecha corta para popups del mapa (sin caracteres raros de AM/PM). */
  function formatMapDate(value) {
    return formatDate(value);
  }

  function buildMapPickPopup(title, lines) {
    const rows = (lines || [])
      .filter(Boolean)
      .map(
        (line) =>
          `<p style="margin:0 0 4px;font-size:12px;line-height:1.35;color:#cbd5e1;">${escapeHtml(line)}</p>`
      )
      .join("");
    return `<div class="map-info-card" style="background:#1e293b;color:#f1f5f9;border-radius:10px;padding:10px 12px;font-family:system-ui,-apple-system,sans-serif;max-width:220px;box-shadow:0 4px 16px rgba(0,0,0,.35);">
      <p style="margin:0 0 6px;font-size:13px;font-weight:700;color:#f8fafc;">${escapeHtml(title)}</p>
      ${rows}
    </div>`;
  }

  function buildMapZonePopup(commune, riskLevel, reportId) {
    return buildMapPickPopup(`Zona ${commune || ""}`, [
      `Riesgo: ${riskLevel || "-"}`,
      `Reporte vinculado: ${reportId != null ? reportId : "-"}`
    ]);
  }

  function mediaItemUrl(item) {
    if (!item) return "";
    return item.url || item.publicUrl || item.storageUrl || "";
  }

  function indexMediaByReportAndPet(mediaList) {
    const byReport = Object.create(null);
    const byPet = Object.create(null);

    (mediaList || []).forEach((item) => {
      const url = mediaItemUrl(item);
      if (!url) return;
      const reportId = Number(item.reportId);
      const petId = Number(item.petId);
      if (Number.isFinite(reportId) && reportId > 0) {
        if (!byReport[reportId]) byReport[reportId] = [];
        byReport[reportId].push(item);
      }
      if (Number.isFinite(petId) && petId > 0) {
        if (!byPet[petId]) byPet[petId] = [];
        byPet[petId].push(item);
      }
    });

    return {
      byReport,
      byPet,
      imageForReport(report) {
        const reportId = Number(report && report.id);
        const petId = Number(report && report.petId);
        const fromReport = byReport[reportId];
        if (fromReport && fromReport.length) return mediaItemUrl(fromReport[0]);
        const fromPet = byPet[petId];
        if (fromPet && fromPet.length) return mediaItemUrl(fromPet[0]);
        return "";
      }
    };
  }

  function normalizeReportStatusKey(status) {
    const s = String(status || "").toUpperCase();
    if (s === "ABIERTO" || s === "OPEN") return "OPEN";
    if (s === "RESUELTO" || s === "RESOLVED") return "RESOLVED";
    if (s === "CERRADO" || s === "CLOSED") return "CLOSED";
    return s || "OPEN";
  }

  /** Tipo mostrado: perdida resuelta/cerrada se muestra como encontrada. */
  function effectiveReportType(report) {
    if (!report) return "";
    const t = String(report.type || "").toUpperCase();
    const s = normalizeReportStatusKey(report.status);
    if ((t === "LOST" || t === "PERDIDA") && (s === "RESOLVED" || s === "CLOSED")) {
      return "FOUND";
    }
    return t;
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
    if (s === "RESOLVED" || s === "RESUELTO") return "Resuelto";
    return status || "—";
  }

  function petNameById(petId, pets) {
    const list = pets || [];
    const pet = list.find((p) => String(p.id) === String(petId));
    if (!pet) return petId ? `#${petId}` : "—";
    return pet.name ? String(pet.name) : `#${pet.id}`;
  }

  function buildMapReportPopup(report, options) {
    const opts = options || {};
    const imageUrl = opts.imageUrl || "";
    const showId = opts.showId !== false;
    const petName = opts.petName || "";
    const typeKey = effectiveReportType(report);
    const typeLabel = reportTypeLabel(typeKey);
    const badgeClass =
      typeKey === "LOST" || typeKey === "PERDIDA"
        ? "map-report-popup__badge--lost"
        : typeKey === "FOUND" || typeKey === "ENCONTRADA"
          ? "map-report-popup__badge--found"
          : "map-report-popup__badge--default";

    const hasImg = Boolean(imageUrl);
    const imgHtml = hasImg
      ? `<div class="map-report-popup__media"><img src="${mediaSrcAttr(imageUrl)}" alt="" loading="lazy" decoding="async" class="map-report-popup__img" onerror="this.closest('.map-report-popup').classList.remove('map-report-popup--has-img');this.parentElement.remove();" /></div>`
      : "";

    const idHtml = showId
      ? `<span class="map-report-popup__id">#${escapeHtml(String(report.id || "-"))}</span>`
      : "";

    const titleHtml = petName
      ? `<span class="map-report-popup__pet">${escapeHtml(petName)}</span>`
      : "";

    const metaParts = [
      report.commune || "Sin comuna",
      reportStatusLabel(report.status),
      report.healthStatus || ""
    ].filter(Boolean);
    const metaLine = metaParts.join(" · ");

    const descHtml = report.description
      ? `<p class="map-report-popup__desc">${escapeHtml(truncate(report.description, 110))}</p>`
      : "";

    const dateHtml = report.createdAt
      ? `<p class="map-report-popup__date">${escapeHtml(formatMapDate(report.createdAt))}</p>`
      : "";

    const contactHtml = opts.contactHtml || "";

    const layoutClass = hasImg ? " map-report-popup--has-img" : "";

    return `<div class="map-report-popup map-info-card${layoutClass}" style="background:#1e293b;color:#f1f5f9;">
      ${imgHtml}
      <div class="map-report-popup__body">
        <div class="map-report-popup__head">
          <span class="map-report-popup__badge ${badgeClass}">${escapeHtml(typeLabel)}</span>
          ${titleHtml}
          ${idHtml}
        </div>
        <p class="map-report-popup__meta-line">${escapeHtml(metaLine)}</p>
        ${descHtml}
        ${dateHtml}
        ${contactHtml}
      </div>
    </div>`;
  }

  function forbiddenUrl() {
    if (window.SANOS_PATHS && typeof window.SANOS_PATHS.page === "function") {
      return window.SANOS_PATHS.root() + "pages/acceso-denegado.html";
    }
    return "./pages/acceso-denegado.html";
  }

  const mapReportPopupLeafletOpts = {
    maxWidth: 220,
    minWidth: 180,
    className: "map-report-popup-wrap"
  };

  function refreshIcons() {
    if (window.lucide && typeof lucide.createIcons === "function") {
      lucide.createIcons();
    }
  }

  function navPage(file) {
    if (window.SANOS_PATHS && typeof window.SANOS_PATHS.page === "function") {
      return window.SANOS_PATHS.page(file);
    }
    if (file.startsWith("admin-")) return "./pages/admin/" + file;
    if (file.startsWith("citizen-")) return "./pages/citizen/" + file;
    return "./" + file;
  }

  function indexUrl() {
    if (window.SANOS_PATHS && typeof window.SANOS_PATHS.index === "function") {
      return window.SANOS_PATHS.index();
    }
    return "./index.html";
  }

  window.SANOS_CORE = {
    storageKeys,
    inferApiBaseUrl,
    sanitizeBaseUrl,
    loadSettings,
    saveSettings,
    readSession,
    writeSession,
    clearSession,
    normalizeToken,
    isUnauthorizedError,
    redirectToLogin,
    api,
    apiUpload,
    getApiBaseUrl,
    mediaUrl,
    mediaSrcAttr,
    mediaItemUrl,
    indexMediaByReportAndPet,
    reportTypeLabel,
    effectiveReportType,
    reportStatusLabel,
    petNameById,
    buildMapReportPopup,
    buildMapPickPopup,
    buildMapZonePopup,
    formatMapDate,
    mapReportPopupLeafletOpts,
    escapeHtml,
    truncate,
    formatDate,
    refreshIcons,
    navPage,
    indexUrl,
    forbiddenUrl
  };
})();
