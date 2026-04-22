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

  async function api(path, options) {
    const opts = options || {};
    const settings = loadSettings();
    const method = opts.method || "GET";
    const headers = { "Content-Type": "application/json" };

    if (opts.auth !== false && opts.token) {
      headers.Authorization = `Bearer ${opts.token}`;
    }

    const response = await fetch(`${settings.apiBaseUrl}${path}`, {
      method,
      headers,
      body: opts.body ? JSON.stringify(opts.body) : undefined
    });

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
      const message =
        (data && data.error) ||
        (typeof data === "string" ? data : "") ||
        `Error ${response.status} en ${path}`;
      throw new Error(message);
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

    return date.toLocaleString();
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
    api,
    escapeHtml,
    truncate,
    formatDate
  };
})();
