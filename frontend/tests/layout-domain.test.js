import { describe, expect, it } from "vitest";
import {
  detectRole,
  pageTitle,
  resolveRedirect
} from "../src/lib/layout-domain.js";

describe("layout-domain", () => {
  it("redirige páginas legacy", () => {
    expect(resolveRedirect("citizen-mascotas.html")).toBe("citizen-reporte.html");
    expect(resolveRedirect("admin-matching.html")).toBe("admin-operaciones.html");
    expect(resolveRedirect("citizen-mapa.html")).toBeNull();
  });

  it("títulos por rol y página", () => {
    expect(pageTitle("admin", "resumen")).toBe("Resumen operativo");
    expect(pageTitle("citizen", "mapa")).toBe("Mapa de la comunidad");
  });

  it("detecta rol desde clases del body", () => {
    expect(detectRole(["dash-page", "admin-page"], {})).toBe("admin");
    expect(detectRole(["dash-page", "citizen-page"], {})).toBe("citizen");
    expect(detectRole(["dash-page"], {})).toBeNull();
  });
});
