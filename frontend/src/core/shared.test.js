import { describe, it, expect, beforeEach } from "vitest";
import "./shared.js";

describe("Frontend - shared.js", () => {
  beforeEach(() => {
    localStorage.clear();
  });

  it("1. normalizeToken elimina Bearer", () => {
    expect(window.SANOS_CORE.normalizeToken("Bearer abc123")).toBe("abc123");
  });

  it("2. normalizeToken retorna vacío si no hay token", () => {
    expect(window.SANOS_CORE.normalizeToken("")).toBe("");
  });

  it("3. escapeHtml limpia código HTML", () => {
    expect(window.SANOS_CORE.escapeHtml("<script>")).toBe("&lt;script&gt;");
  });

  it("4. truncate corta textos largos", () => {
    expect(window.SANOS_CORE.truncate("Hola mundo", 5)).toBe("Hola...");
  });

  it("5. reportTypeLabel traduce LOST", () => {
    expect(window.SANOS_CORE.reportTypeLabel("LOST")).toBe("Perdida");
  });

  it("6. writeSession y readSession guardan sesión", () => {
    window.SANOS_CORE.writeSession("citizen", {
      token: "abc123",
      user: { name: "Juan" }
    });

    const session = window.SANOS_CORE.readSession("citizen");

    expect(session.token).toBe("abc123");
    expect(session.user.name).toBe("Juan");
  });
});