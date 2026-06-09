import { describe, expect, it } from "vitest";
import { formatDate, parseApiDateTime } from "../src/lib/datetime-utils.js";

describe("datetime-utils", () => {
  it("parsea ISO con Z como UTC", () => {
    const d = parseApiDateTime("2026-05-25T15:30:00Z");
    expect(d).toBeInstanceOf(Date);
    expect(d.getUTCHours()).toBe(15);
  });

  it("parsea LocalDateTime sin offset como UTC", () => {
    const d = parseApiDateTime("2026-05-25 15:30:00");
    expect(d).toBeInstanceOf(Date);
    expect(d.getUTCHours()).toBe(15);
  });

  it("formatea fecha en zona Chile", () => {
    const text = formatDate("2026-05-25T15:30:00Z", "America/Santiago");
    expect(text).toMatch(/25/);
    expect(text).not.toBe("-");
  });

  it("retorna guion si valor vacío", () => {
    expect(formatDate("")).toBe("-");
    expect(parseApiDateTime("")).toBeNull();
  });
});
