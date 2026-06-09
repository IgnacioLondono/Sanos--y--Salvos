/** Índice de medios por reporte y mascota. */

export function mediaItemUrl(item) {
  if (!item) return "";
  return item.url || item.publicUrl || item.storageUrl || "";
}

export function indexMediaByReportAndPet(mediaList) {
  const byReport = Object.create(null);
  const byPet = Object.create(null);

  (mediaList || []).forEach((item) => {
    const url = mediaItemUrl(item);
    if (!url) return;
    const reportId = Number(item.reportId);
    const petId = Number(item.petId);
    if (Number.isFinite(reportId) && reportId > 0) {
      if (!byReport[reportId]) byReport[reportId] = [];
      byReport[reportId].push(item);
    }
    if (Number.isFinite(petId) && petId > 0) {
      if (!byPet[petId]) byPet[petId] = [];
      byPet[petId].push(item);
    }
  });

  return {
    byReport,
    byPet,
    imageForReport(report) {
      const reportId = Number(report && report.id);
      const petId = Number(report && report.petId);
      const fromReport = byReport[reportId];
      if (fromReport && fromReport.length) return mediaItemUrl(fromReport[0]);
      const fromPet = byPet[petId];
      if (fromPet && fromPet.length) return mediaItemUrl(fromPet[0]);
      return "";
    }
  };
}
