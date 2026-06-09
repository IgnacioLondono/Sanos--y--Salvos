import { describe, expect, it } from "vitest";
import { nextTheme, normalizeTheme, themeToggleLabel } from "../src/lib/theme-domain.js";

describe("theme-domain", () => {
  it("alterna entre claro y oscuro", () => {
    expect(nextTheme("dark")).toBe("light");
    expect(nextTheme("light")).toBe("dark");
  });

  it("normaliza valores de tema", () => {
    expect(normalizeTheme("dark")).toBe("dark");
    expect(normalizeTheme("invalid")).toBe("light");
  });

  it("etiqueta accesible del botón tema", () => {
    expect(themeToggleLabel(true)).toContain("claro");
    expect(themeToggleLabel(false)).toContain("oscuro");
  });
});
