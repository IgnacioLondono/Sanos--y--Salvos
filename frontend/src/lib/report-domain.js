/** Dominio de reportes (perdida / encontrada / estados). */

export function normalizeReportStatusKey(status) {
  const s = String(status || "").toUpperCase();
  if (s === "ABIERTO" || s === "OPEN") return "OPEN";
  if (s === "RESUELTO" || s === "RESOLVED") return "RESOLVED";
  if (s === "CERRADO" || s === "CLOSED") return "CLOSED";
  return s || "OPEN";
}

export function effectiveReportType(report) {
  if (!report) return "";
  const t = String(report.type || "").toUpperCase();
  const s = normalizeReportStatusKey(report.status);
  if ((t === "LOST" || t === "PERDIDA") && (s === "RESOLVED" || s === "CLOSED")) {
    return "FOUND";
  }
  return t;
}

export function reportTypeLabel(type) {
  const t = String(type || "").toUpperCase();
  if (t === "LOST" || t === "PERDIDA") return "Perdida";
  if (t === "FOUND" || t === "ENCONTRADA") return "Encontrada";
  return type || "—";
}

export function reportStatusLabel(status) {
  const s = String(status || "").toUpperCase();
  if (s === "OPEN" || s === "ABIERTO") return "Abierto";
  if (s === "CLOSED" || s === "CERRADO") return "Cerrado";
  if (s === "RESOLVED" || s === "RESUELTO") return "Resuelto";
  return status || "—";
}

export function reportQuickAction(report) {
  const status = normalizeReportStatusKey(report.status);
  const type = String(report.type || "").toUpperCase();
  const effective = effectiveReportType(report);
  if (status === "OPEN") {
    const isLost =
      effective === "LOST" ||
      effective === "PERDIDA" ||
      type === "LOST" ||
      type === "PERDIDA";
    return {
      label: isLost ? "Marcar encontrado" : "Marcar cerrado",
      status: isLost ? "RESOLVED" : "CLOSED"
    };
  }
  if (status === "RESOLVED" || status === "CLOSED") {
    const canMarkLostAgain =
      ((type === "FOUND" || type === "ENCONTRADA") && status === "RESOLVED") ||
      ((type === "LOST" || type === "PERDIDA") && (status === "RESOLVED" || status === "CLOSED"));
    if (canMarkLostAgain) {
      const revertType = type === "FOUND" || type === "ENCONTRADA" ? "LOST" : null;
      return { label: "Marcar perdida", status: "OPEN", type: revertType };
    }
    return { label: "Reabrir", status: "OPEN" };
  }
  return null;
}

export function petNameById(petId, pets) {
  const list = pets || [];
  const pet = list.find((p) => String(p.id) === String(petId));
  if (!pet) return petId ? `#${petId}` : "—";
  return pet.name ? String(pet.name) : `#${pet.id}`;
}
