import { describe, expect, it } from "vitest";
import {
  apiErrorMessage,
  inferApiBaseUrl,
  isUnauthorizedError,
  mediaUrl,
  normalizeToken,
  sanitizeBaseUrl
} from "../src/lib/api-utils.js";

describe("api-utils", () => {
  it("sanitiza URL base sin barra final", () => {
    expect(sanitizeBaseUrl("http://localhost:8080/")).toBe("http://localhost:8080");
  });

  it("infiere API base desde host", () => {
    expect(inferApiBaseUrl("http:", "localhost")).toBe("http://localhost:8080");
  });

  it("normaliza token Bearer", () => {
    expect(normalizeToken("Bearer abc.def")).toBe("abc.def");
  });

  it("mensajes de error por código HTTP", () => {
    expect(apiErrorMessage(401, "/api/iam/login", {})).toContain("correo");
    expect(apiErrorMessage(429, "/api/reports", {})).toContain("Demasiadas");
  });

  it("detecta error 401", () => {
    expect(isUnauthorizedError({ status: 401 })).toBe(true);
    expect(isUnauthorizedError({ message: "Error 401" })).toBe(true);
  });

  it("resuelve URL de media relativa y absoluta", () => {
    const base = "http://localhost:8080";
    expect(mediaUrl("https://cdn/img.jpg", base)).toBe("https://cdn/img.jpg");
    expect(mediaUrl("/api/media/1", base)).toBe("http://localhost:8080/api/media/1");
  });
});
