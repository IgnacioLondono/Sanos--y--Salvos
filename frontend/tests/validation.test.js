import { describe, expect, it } from "vitest";
import {
  isStrongPassword,
  isValidChilePhone,
  isValidEmail,
  normalizeRut,
  validateRegistration
} from "../src/lib/validation.js";

const validModel = () => ({
  fullName: "Ana Perez Lopez",
  rutDocument: "12345678-9",
  email: "ana@test.cl",
  password: "Segura#2026",
  passwordConfirm: "Segura#2026",
  commune: "Providencia",
  phone: "+56 9 1234 5678",
  address: "Av. Providencia 100",
  emergencyContactName: "Juan Perez",
  emergencyContactPhone: "+56 9 9876 5432",
  acceptTerms: true,
  acceptPrivacy: true
});

describe("validation", () => {
  it("normaliza RUT sin puntos", () => {
    expect(normalizeRut("12.345.678-k")).toBe("12345678-K");
  });

  it("acepta registro válido sin errores", () => {
    expect(validateRegistration(validModel())).toEqual([]);
  });

  it("rechaza correo inválido", () => {
    const errors = validateRegistration({ ...validModel(), email: "mal" });
    expect(errors).toContain("Correo invalido.");
  });

  it("rechaza contraseña débil", () => {
    const errors = validateRegistration({ ...validModel(), password: "123", passwordConfirm: "123" });
    expect(errors.some((e) => e.includes("contraseña"))).toBe(true);
  });

  it("rechaza teléfonos con formato incorrecto", () => {
    expect(isValidChilePhone("+56 2 1234 5678")).toBe(false);
    expect(isValidChilePhone("+56 9 1234 5678")).toBe(true);
  });

  it("valida email y contraseña fuerte", () => {
    expect(isValidEmail("user@mail.cl")).toBe(true);
    expect(isStrongPassword("Segura#2026")).toBe(true);
    expect(isStrongPassword("corta")).toBe(false);
  });

  it("rechaza términos no aceptados y contraseñas distintas", () => {
    expect(validateRegistration({ ...validModel(), acceptTerms: false }).length).toBeGreaterThan(0);
    expect(
      validateRegistration({ ...validModel(), passwordConfirm: "Otra#2026" })
    ).toContain("Las contraseñas no coinciden.");
  });

  it("rechaza nombre corto y comuna inválida", () => {
    expect(validateRegistration({ ...validModel(), fullName: "Ana" }).length).toBeGreaterThan(0);
    expect(validateRegistration({ ...validModel(), commune: "ab" }).length).toBeGreaterThan(0);
  });
});
