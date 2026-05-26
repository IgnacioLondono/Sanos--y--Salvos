import fs from "fs";
import path from "path";
import { fileURLToPath } from "url";

const __dirname = path.dirname(fileURLToPath(import.meta.url));
const file = path.join(__dirname, "..", "pages", "citizen", "citizen-reporte.html");
let html = fs.readFileSync(file, "utf8");

const pairs = [
  ["ubicaci\uFFFDn", "ubicación"],
  ["identificaci\uFFFDn", "identificación"],
  ["Tama\uFFFDo", "Tamaño"],
  ["Peque\uFFFDo", "Pequeño"],
  ["d\uFFFDnde", "dónde"],
  ["ocurri\uFFFD", "ocurrió"],
  ["selecci\uFFFDn", "selección"],
  ["Descripci\uFFFDn", "Descripción"],
  ["se\uFFFDas", "señas"],
  ["fotogr\uFFFDfica", "fotográfica"],
  ["autom\uFFFDticamente", "automáticamente"],
  ["sesi\uFFFDn", "sesión"],
  ["tama\uFFFDo", "tamaño"],
  ["\uFFFD", "—"]
];

for (const [bad, good] of pairs) {
  html = html.split(bad).join(good);
}

fs.writeFileSync(file, html, "utf8");
console.log("OK:", file);
