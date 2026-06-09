import { describe, expect, it } from "vitest";
import { normalizeProfile, profileLocationLabel } from "../src/lib/profile-domain.js";

describe("profile-domain", () => {
  it("normaliza perfil desde API IAM", () => {
    const p = normalizeProfile({
      id: 1,
      email: "u@mail.cl",
      fullName: "Ana Perez",
      displayName: "Ana",
      rut: "12345678-9",
      commune: "Santiago",
      phone: "+56 9 1111 2222"
    });
    expect(p.email).toBe("u@mail.cl");
    expect(p.fullName).toBe("Ana Perez");
    expect(p.commune).toBe("Santiago");
  });

  it("acepta alias snake_case del backend", () => {
    const p = normalizeProfile({
      full_name: "Juan",
      rut_document: "1-9",
      comuna: "Nunoa"
    });
    expect(p.fullName).toBe("Juan");
    expect(p.rut).toBe("1-9");
    expect(p.commune).toBe("Nunoa");
  });

  it("arma etiqueta de ubicación", () => {
    expect(profileLocationLabel({ commune: "Providencia", address: "Calle 1" })).toBe(
      "Providencia · Calle 1"
    );
    expect(profileLocationLabel({})).toBe("—");
  });
});
