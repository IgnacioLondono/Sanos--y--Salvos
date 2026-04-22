(function () {
  const core = window.SANOS_CORE;

  const els = {
    fullName: document.getElementById("fullName"),
    rutDocument: document.getElementById("rutDocument"),
    email: document.getElementById("email"),
    password: document.getElementById("password"),
    commune: document.getElementById("commune"),
    phone: document.getElementById("phone"),
    address: document.getElementById("address"),
    emergencyContactName: document.getElementById("emergencyContactName"),
    emergencyContactPhone: document.getElementById("emergencyContactPhone"),
    acceptTerms: document.getElementById("acceptTerms"),
    acceptPrivacy: document.getElementById("acceptPrivacy"),
    btnRegister: document.getElementById("btnRegister"),
    status: document.getElementById("registerStatus")
  };

  init();

  function init() {
    const citizenSession = core.readSession("citizen");
    if (citizenSession.token && citizenSession.user) {
      window.location.href = "./citizen-dashboard.html";
      return;
    }

    els.btnRegister.addEventListener("click", onRegister);
  }

  async function onRegister() {
    const fullName = els.fullName.value.trim();
    const rutDocument = normalizeRut(els.rutDocument.value);
    const email = els.email.value.trim().toLowerCase();
    const password = els.password.value.trim();
    const commune = els.commune.value.trim();
    const phone = els.phone.value.trim();
    const address = els.address.value.trim();
    const emergencyContactName = els.emergencyContactName.value.trim();
    const emergencyContactPhone = els.emergencyContactPhone.value.trim();

    const errors = validateRegistration({
      fullName,
      rutDocument,
      email,
      password,
      commune,
      phone,
      address,
      emergencyContactName,
      emergencyContactPhone,
      acceptTerms: els.acceptTerms.checked,
      acceptPrivacy: els.acceptPrivacy.checked
    });

    if (errors.length) {
      return showStatus(errors[0], true);
    }

    const displayName = fullName.split(" ").slice(0, 2).join(" ");

    try {
      await core.api("/api/iam/register", {
        method: "POST",
        auth: false,
        body: {
          fullName,
          rutDocument,
          email,
          password,
          displayName,
          commune,
          phone,
          address,
          emergencyContactName,
          emergencyContactPhone,
          acceptedTerms: true,
          acceptedPrivacyPolicy: true,
          role: "CITIZEN"
        }
      });

      showStatus("Cuenta creada correctamente. Redirigiendo al login...");
      window.setTimeout(() => {
        window.location.href = "./index.html";
      }, 900);
    } catch (error) {
      showStatus(error.message, true);
    }
  }

  function showStatus(message, isError) {
    els.status.textContent = message;
    els.status.style.color = isError ? "#b74f4f" : "";
  }

  function normalizeRut(value) {
    return String(value || "").trim().replace(/\./g, "").toUpperCase();
  }

  function validateRegistration(model) {
    const errors = [];

    if (!model.fullName || model.fullName.length < 6 || model.fullName.split(" ").length < 2) {
      errors.push("Ingresa nombre y apellido validos.");
    }

    if (!/^\d{7,8}-[\dK]$/.test(model.rutDocument)) {
      errors.push("RUT invalido. Usa formato 12345678-9.");
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(model.email)) {
      errors.push("Correo invalido.");
    }

    if (!isStrongPassword(model.password)) {
      errors.push("La contrasena debe tener minimo 10 caracteres, mayuscula, minuscula, numero y simbolo.");
    }

    if (!model.commune || model.commune.length < 3) {
      errors.push("Debes indicar una comuna valida.");
    }

    if (!/^\+?56\s?9\s?\d{4}\s?\d{4}$/.test(model.phone)) {
      errors.push("Telefono invalido. Usa formato +56 9 1234 5678.");
    }

    if (!model.address || model.address.length < 8) {
      errors.push("Ingresa una direccion referencial valida.");
    }

    if (!model.emergencyContactName || model.emergencyContactName.length < 5) {
      errors.push("Ingresa nombre de contacto de emergencia.");
    }

    if (!/^\+?56\s?9\s?\d{4}\s?\d{4}$/.test(model.emergencyContactPhone)) {
      errors.push("Telefono de emergencia invalido. Usa formato +56 9 9876 5432.");
    }

    if (!model.acceptTerms || !model.acceptPrivacy) {
      errors.push("Debes aceptar terminos y politica de privacidad.");
    }

    return errors;
  }

  function isStrongPassword(password) {
    return (
      typeof password === "string" &&
      password.length >= 10 &&
      /[A-Z]/.test(password) &&
      /[a-z]/.test(password) &&
      /\d/.test(password) &&
      /[^A-Za-z0-9]/.test(password)
    );
  }
})();
