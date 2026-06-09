import { describe, expect, it } from "vitest";
import { indexMediaByReportAndPet, mediaItemUrl } from "../src/lib/media-utils.js";

describe("media-utils", () => {
  it("obtiene URL pública del item", () => {
    expect(mediaItemUrl({ url: "http://x/a.jpg" })).toBe("http://x/a.jpg");
    expect(mediaItemUrl({ publicUrl: "http://x/b.jpg" })).toBe("http://x/b.jpg");
  });

  it("indexa medios por reporte y mascota", () => {
    const idx = indexMediaByReportAndPet([
      { reportId: 10, petId: 3, url: "http://img/1.jpg" },
      { reportId: 10, petId: 3, url: "http://img/2.jpg" }
    ]);
    expect(idx.byReport[10]).toHaveLength(2);
    expect(idx.imageForReport({ id: 10, petId: 3 })).toBe("http://img/1.jpg");
    expect(idx.imageForReport({ id: 99, petId: 3 })).toBe("http://img/1.jpg");
  });
});
