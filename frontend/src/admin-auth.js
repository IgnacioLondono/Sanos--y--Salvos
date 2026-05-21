(function () {
  const core = window.SANOS_CORE;

  const ADMIN_EMAIL = "admin@sanosysalvos.cl";
  const ADMIN_PASSWORD = "Admin#Sanos2026";

  const els = {
    email: document.getElementById("adminEmail"),
    password: document.getElementById("adminPassword"),
    btnLogin: document.getElementById("btnAdminLogin"),
    status: document.getElementById("adminAuthStatus")
  };

  init();

  function init() {
    const existing = core.readSession("admin");
    if (existing.token && existing.user) {
      window.location.href = "./admin-resumen.html";
      return;
    }

    els.email.value = ADMIN_EMAIL;
    els.btnLogin.addEventListener("click", onAdminLogin);
  }

  async function onAdminLogin() {
    const email = els.email.value.trim().toLowerCase();
    const password = els.password.value.trim();

    if (email !== ADMIN_EMAIL || password !== ADMIN_PASSWORD) {
      return showStatus("Credenciales admin invalidas.", true);
    }

    try {
      const data = await ensureAdminInIam(email, password);
      core.writeSession("admin", {
        token: data.token,
        user: {
          id: data.id,
          email: data.email,
          displayName: data.displayName || "Administrador",
          role: "ADMIN"
        }
      });

      showStatus("Acceso admin concedido. Redirigiendo...");
      window.location.href = "./admin-resumen.html";
    } catch (error) {
      showStatus(error.message, true);
    }
  }

  async function ensureAdminInIam(email, password) {
    try {
      return await core.api("/api/iam/login", {
        method: "POST",
        auth: false,
        body: { email, password }
      });
    } catch (loginError) {
      await core.api("/api/iam/register", {
        method: "POST",
        auth: false,
        body: {
          email,
          password,
          displayName: "Administrador Sanos",
          phone: "",
          role: "ADMIN"
        }
      });

      return core.api("/api/iam/login", {
        method: "POST",
        auth: false,
        body: { email, password }
      });
    }
  }

  function showStatus(message, isError) {
    els.status.textContent = message;
    els.status.classList.toggle("is-error", Boolean(isError));
  }
})();
