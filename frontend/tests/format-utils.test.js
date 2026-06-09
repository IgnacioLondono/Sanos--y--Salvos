import { describe, expect, it } from "vitest";
import { escapeHtml, truncate } from "../src/lib/format-utils.js";

describe("format-utils", () => {
  it("escapa caracteres HTML peligrosos", () => {
    expect(escapeHtml('<script>"x"</script>')).toBe(
      "&lt;script&gt;&quot;x&quot;&lt;/script&gt;"
    );
  });

  it("trunca texto largo", () => {
    expect(truncate("abcdef", 4)).toBe("abc...");
    expect(truncate("ab", 10)).toBe("ab");
  });
});
