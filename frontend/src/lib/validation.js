/** Validación de registro y credenciales. */

export function normalizeRut(value) {
  return String(value || "").trim().replace(/\./g, "").toUpperCase();
}

export function isStrongPassword(password) {
  return (
    typeof password === "string" &&
    password.length >= 10 &&
    /[A-Z]/.test(password) &&
    /[a-z]/.test(password) &&
    /\d/.test(password) &&
    /[^A-Za-z0-9]/.test(password)
  );
}

export function isValidEmail(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]{2,}$/.test(String(email || ""));
}

export function isValidChilePhone(phone) {
  return /^\+?56\s?9\s?\d{4}\s?\d{4}$/.test(String(phone || ""));
}

export function validateRegistration(model) {
  const errors = [];
  const m = model || {};

  if (!m.fullName || m.fullName.length < 6 || m.fullName.split(" ").length < 2) {
    errors.push("Ingresa nombre y apellido validos.");
  }
  if (!/^\d{7,8}-[\dK]$/.test(m.rutDocument || "")) {
    errors.push("RUT invalido. Usa formato 12345678-9.");
  }
  if (!isValidEmail(m.email)) {
    errors.push("Correo invalido.");
  }
  if (!isStrongPassword(m.password)) {
    errors.push("La contraseña debe tener mínimo 10 caracteres, mayúscula, minúscula, número y símbolo.");
  }
  if (m.password !== m.passwordConfirm) {
    errors.push("Las contraseñas no coinciden.");
  }
  if (!m.commune || m.commune.length < 3) {
    errors.push("Debes indicar una comuna valida.");
  }
  if (!isValidChilePhone(m.phone)) {
    errors.push("Telefono invalido. Usa formato +56 9 1234 5678.");
  }
  if (!m.address || m.address.length < 8) {
    errors.push("Ingresa una direccion referencial valida.");
  }
  if (!m.emergencyContactName || m.emergencyContactName.length < 5) {
    errors.push("Ingresa nombre de contacto de emergencia.");
  }
  if (!isValidChilePhone(m.emergencyContactPhone)) {
    errors.push("Telefono de emergencia invalido. Usa formato +56 9 9876 5432.");
  }
  if (!m.acceptTerms || !m.acceptPrivacy) {
    errors.push("Debes aceptar terminos y politica de privacidad.");
  }
  return errors;
}
