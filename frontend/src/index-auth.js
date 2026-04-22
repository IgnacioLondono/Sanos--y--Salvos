(function () {
  const core = window.SANOS_CORE;

  const els = {
    email: document.getElementById("email"),
    password: document.getElementById("password"),
    btnLogin: document.getElementById("btnLogin"),
    authStatus: document.getElementById("authStatus")
  };

  init();

  function init() {
    const citizenSession = core.readSession("citizen");
    if (citizenSession.token && citizenSession.user) {
      window.location.href = "./citizen-dashboard.html";
      return;
    }

    const adminSession = core.readSession("admin");
    if (adminSession.token && adminSession.user) {
      window.location.href = "./admin-dashboard.html";
      return;
    }

    els.btnLogin.addEventListener("click", onLogin);
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

      const user = {
        id: data.id,
        email: data.email,
        displayName: data.displayName,
        role: data.role || "CITIZEN"
      };

      if (String(user.role).toUpperCase() === "ADMIN") {
        core.clearSession("citizen");
        core.writeSession("admin", { token: data.token, user });
        showStatus("Sesion admin iniciada. Redirigiendo...");
        window.location.href = "./admin-dashboard.html";
        return;
      }

      core.clearSession("admin");
      core.writeSession("citizen", { token: data.token, user });

      showStatus("Sesion iniciada. Redirigiendo al dashboard ciudadano...");
      window.location.href = "./citizen-dashboard.html";
    } catch (error) {
      showStatus(error.message, true);
    }
  }

  function showStatus(message, isError) {
    els.authStatus.textContent = message;
    els.authStatus.style.color = isError ? "#b74f4f" : "";
  }
})();
