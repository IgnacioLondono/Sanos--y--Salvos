import { describe, expect, it } from "vitest";
import {
  effectiveReportType,
  normalizeReportStatusKey,
  petNameById,
  reportQuickAction,
  reportStatusLabel,
  reportTypeLabel
} from "../src/lib/report-domain.js";

describe("report-domain", () => {
  it("normaliza estados en español e inglés", () => {
    expect(normalizeReportStatusKey("ABIERTO")).toBe("OPEN");
    expect(normalizeReportStatusKey("RESUELTO")).toBe("RESOLVED");
    expect(normalizeReportStatusKey("CLOSED")).toBe("CLOSED");
  });

  it("perdida resuelta se muestra como encontrada", () => {
    const report = { type: "LOST", status: "RESOLVED" };
    expect(effectiveReportType(report)).toBe("FOUND");
  });

  it("perdida abierta sigue siendo perdida", () => {
    expect(effectiveReportType({ type: "LOST", status: "OPEN" })).toBe("LOST");
  });

  it("etiquetas de tipo y estado en español", () => {
    expect(reportTypeLabel("LOST")).toBe("Perdida");
    expect(reportTypeLabel("FOUND")).toBe("Encontrada");
    expect(reportStatusLabel("OPEN")).toBe("Abierto");
    expect(reportStatusLabel("RESOLVED")).toBe("Resuelto");
  });

  it("acción rápida marcar encontrado en reporte perdido abierto", () => {
    const action = reportQuickAction({ type: "LOST", status: "OPEN" });
    expect(action.label).toBe("Marcar encontrado");
    expect(action.status).toBe("RESOLVED");
  });

  it("acción rápida marcar perdida tras encontrado", () => {
    const action = reportQuickAction({ type: "FOUND", status: "RESOLVED" });
    expect(action.label).toBe("Marcar perdida");
    expect(action.status).toBe("OPEN");
    expect(action.type).toBe("LOST");
  });

  it("resuelve nombre de mascota por id", () => {
    const pets = [{ id: 3, name: "Milo" }];
    expect(petNameById(3, pets)).toBe("Milo");
    expect(petNameById(99, pets)).toBe("#99");
  });
});
