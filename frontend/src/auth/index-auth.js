(function () {
  if (!window.SANOS_LOGIN_NO_AUTO_REDIRECT) {
    window.SANOS_LOGIN_NO_AUTO_REDIRECT = true;
  }

  const core = window.SANOS_CORE;

  const els = {
    email: document.getElementById("email"),
    password: document.getElementById("password"),
    btnLogin: document.getElementById("btnLogin"),
    authStatus: document.getElementById("authStatus"),
    authCard: document.querySelector(".auth-card-min")
  };

  init();

  function init() {
    handleLogoutQuery();

    const citizenSession = core.readSession("citizen");
    const adminSession = core.readSession("admin");
    const active = citizenSession.token
      ? { type: "citizen", session: citizenSession }
      : adminSession.token
        ? { type: "admin", session: adminSession }
        : null;

    if (active) {
      renderActiveSessionNotice(active);
    }

    if (els.btnLogin) {
      els.btnLogin.addEventListener("click", onLogin);
    }

    /* Nunca redirigir automáticamente al dashboard desde index.html */
  }

  function handleLogoutQuery() {
    const params = new URLSearchParams(window.location.search);
    if (!params.has("logout") && !params.has("cerrar")) return;

    core.clearSession("citizen");
    core.clearSession("admin");
    window.history.replaceState({}, "", (window.SANOS_PATHS && SANOS_PATHS.index()) || "./index.html");
    if (els.authStatus) {
      showStatus("Sesión cerrada. Puedes iniciar sesión de nuevo.");
    }
  }

  function renderActiveSessionNotice(active) {
    const user = active.session.user || {};
    const name = user.displayName || user.email || "tu cuenta";
    const dashboard =
      active.type === "admin"
        ? (window.SANOS_PATHS ? SANOS_PATHS.page("admin-resumen.html") : "./pages/admin/admin-resumen.html")
        : (window.SANOS_PATHS ? SANOS_PATHS.page("citizen-reporte.html") : "./pages/citizen/citizen-reporte.html");
    const roleLabel = active.type === "admin" ? "Administrador" : "Ciudadano";

    const notice = document.createElement("div");
    notice.className = "auth-session-notice";
    notice.setAttribute("role", "status");
    notice.innerHTML =
      `<p><strong>Ya tienes sesión activa</strong> (${core.escapeHtml(roleLabel)}: ${core.escapeHtml(name)}).</p>` +
      `<div class="auth-session-notice__actions">` +
      `<a class="btn btn-primary" href="${dashboard}">Ir al panel</a>` +
      `<a class="btn btn-secondary" href="${(window.SANOS_PATHS && SANOS_PATHS.index()) || "./index.html"}?logout=1">Cerrar sesión</a>` +
      `</div>`;

    if (els.authCard) {
      els.authCard.insertBefore(notice, els.authCard.firstChild);
    }
  }

  async function onLogin() {
    const email = els.email.value.trim().toLowerCase();
    const password = els.password.value.trim();

    if (!email || !password) {
      return showStatus("Debes ingresar correo y contrasena.", true);
    }

    try {
      const data = await core.api("/api/iam/login", {
        method: "POST",
        auth: false,
        body: { email, password }
      });

      let user = {
        id: data.id,
        email: data.email,
        displayName: data.displayName,
        role: data.role || "CITIZEN"
      };

      try {
        let profile = null;
        const profilePaths = ["/api/iam/profile", "/api/iam/users/me"];
        for (const path of profilePaths) {
          try {
            profile = await core.api(path, { token: data.token });
            break;
          } catch (e) {
            /* siguiente ruta */
          }
        }
        if (profile) {
          user = {
            id: profile.id,
            email: profile.email,
            displayName: profile.displayName,
            fullName: profile.fullName,
            rut: profile.rut,
            commune: profile.commune,
            phone: profile.phone,
            address: profile.address,
            emergencyContactName: profile.emergencyContactName,
            emergencyContactPhone: profile.emergencyContactPhone,
            role: profile.role || user.role
          };
        }
      } catch (e) {
        /* usar datos del login */
      }

      if (String(user.role).toUpperCase() === "ADMIN") {
        core.clearSession("citizen");
        core.writeSession("admin", { token: data.token, user });
        showStatus("Sesion admin iniciada. Redirigiendo...");
        window.location.href = window.SANOS_PATHS ? SANOS_PATHS.page("admin-resumen.html") : "./pages/admin/admin-resumen.html";
        return;
      }

      core.clearSession("admin");
      core.writeSession("citizen", { token: data.token, user });

      showStatus("Sesion iniciada. Redirigiendo al dashboard ciudadano...");
      window.location.href = window.SANOS_PATHS ? SANOS_PATHS.page("citizen-reporte.html") : "./pages/citizen/citizen-reporte.html";
    } catch (error) {
      showStatus(error.message, true);
    }
  }

  function showStatus(message, isError) {
    if (!els.authStatus) return;
    els.authStatus.textContent = message;
    els.authStatus.classList.toggle("is-error", Boolean(isError));
  }
})();
