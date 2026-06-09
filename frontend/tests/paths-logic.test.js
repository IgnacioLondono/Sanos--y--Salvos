import { describe, expect, it } from "vitest";
import { inPages, resolvePage, rootPrefix } from "../src/lib/paths-logic.js";

describe("paths-logic", () => {
  it("detecta páginas bajo /pages/", () => {
    expect(inPages("/pages/citizen/citizen-mapa.html")).toBe(true);
    expect(inPages("/index.html")).toBe(false);
  });

  it("prefijo raíz según ubicación", () => {
    expect(rootPrefix("/pages/citizen/x.html")).toBe("../../");
    expect(rootPrefix("/index.html")).toBe("./");
  });

  it("resuelve rutas ciudadano desde raíz", () => {
    expect(resolvePage("citizen-mapa.html", "/index.html")).toBe("./pages/citizen/citizen-mapa.html");
  });

  it("resuelve rutas admin desde /pages/citizen", () => {
    expect(resolvePage("admin-resumen.html", "/pages/citizen/citizen-mapa.html")).toBe(
      "../../pages/admin/admin-resumen.html"
    );
  });

  it("resuelve index y register desde pages", () => {
    expect(resolvePage("index.html", "/pages/citizen/x.html")).toBe("../../index.html");
    expect(resolvePage("", "/index.html")).toBe("./");
  });
});
