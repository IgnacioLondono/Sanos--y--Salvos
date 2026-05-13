(function () {
  const core = window.SANOS_CORE;
  const state = {
    session: core.readSession("citizen"),
    pets: [],
    reports: [],
    media: [],
    matching: [],
    dashboard: null,
    map: null,
    markerLayer: null,
    pickMarker: null
  };

  const els = {
    citizenIdentity: document.getElementById("citizenIdentity"),
    citizenKpis: document.getElementById("citizenKpis"),
    citizenStatus: document.getElementById("citizenStatus"),
    btnCitizenLogout: document.getElementById("btnCitizenLogout"),
    btnRefresh: document.getElementById("btnRefresh"),
    petForm: document.getElementById("petForm"),
    reportForm: document.getElementById("reportForm"),
    mediaForm: document.getElementById("mediaForm"),
    petsList: document.getElementById("petsList"),
    reportsList: document.getElementById("reportsList"),
    matchingList: document.getElementById("matchingList"),
    reportPetId: document.getElementById("reportPetId"),
    mediaPetId: document.getElementById("mediaPetId"),
    petChip: document.getElementById("petChip"),
    petName: document.getElementById("petName"),
    petSpecies: document.getElementById("petSpecies"),
    petBreed: document.getElementById("petBreed"),
    petColor: document.getElementById("petColor"),
    petSize: document.getElementById("petSize"),
    reportType: document.getElementById("reportType"),
    reportCommune: document.getElementById("reportCommune"),
    reportHealth: document.getElementById("reportHealth"),
    reportLat: document.getElementById("reportLat"),
    reportLng: document.getElementById("reportLng"),
    reportDescription: document.getElementById("reportDescription"),
    mediaUrl: document.getElementById("mediaUrl"),
    mediaTags: document.getElementById("mediaTags")
  };

  init();

  function init() {
    if (!state.session.token || !state.session.user) {
      window.location.href = "./index.html";
      return;
    }

    els.citizenIdentity.textContent = state.session.user.displayName || state.session.user.email;

    els.btnCitizenLogout.addEventListener("click", onLogout);
    els.btnRefresh.addEventListener("click", refreshAll);
    els.petForm.addEventListener("submit", onCreatePet);
    els.reportForm.addEventListener("submit", onCreateReport);
    els.mediaForm.addEventListener("submit", onCreateMedia);

    initMap();
    refreshAll();
  }

  function initMap() {
    if (typeof L === "undefined") return;
    const settings = core.loadSettings();
    const center = settings.defaultCenter || { lat: -33.4489, lng: -70.6693 };
    state.map = L.map("citizenMap").setView([center.lat, center.lng], 12);
    L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png", {
      maxZoom: 19,
      attribution: "&copy; OpenStreetMap"
    }).addTo(state.map);
    state.markerLayer = L.layerGroup().addTo(state.map);

    state.map.on("click", (event) => {
      const { lat, lng } = event.latlng;
      els.reportLat.value = lat.toFixed(6);
      els.reportLng.value = lng.toFixed(6);
      if (state.pickMarker) {
        state.pickMarker.setLatLng([lat, lng]);
      } else {
        state.pickMarker = L.circleMarker([lat, lng], {
          radius: 9,
          color: "#f1b14c",
          fillColor: "#f1b14c",
          fillOpacity: 0.8
        }).addTo(state.map);
      }
      state.pickMarker.bindPopup(`Ubicacion seleccionada<br/>${lat.toFixed(5)}, ${lng.toFixed(5)}`).openPopup();
    });
  }

  function renderReportsOnMap() {
    if (!state.map || !state.markerLayer) return;
    state.markerLayer.clearLayers();
    const bounds = [];
    state.reports.forEach((report) => {
      if (report.latitude == null || report.longitude == null) return;
      const lat = Number(report.latitude);
      const lng = Number(report.longitude);
      if (Number.isNaN(lat) || Number.isNaN(lng)) return;
      const isLost = String(report.type || "").toUpperCase() === "LOST";
      const color = isLost ? "#c0392b" : "#3d8f73";
      const marker = L.circleMarker([lat, lng], {
        radius: 8,
        color,
        fillColor: color,
        fillOpacity: 0.75
      });
      marker.bindPopup(
        `<strong>${core.escapeHtml(report.type || "Reporte")}</strong><br/>` +
          `${core.escapeHtml(report.commune || "Sin comuna")}<br/>` +
          `<em>${core.escapeHtml(report.description || "Sin descripcion")}</em>`
      );
      marker.addTo(state.markerLayer);
      bounds.push([lat, lng]);
    });
    if (bounds.length) {
      state.map.fitBounds(bounds, { padding: [30, 30], maxZoom: 14 });
    }
  }

  async function refreshAll() {
    try {
      setStatus("Sincronizando datos...");

      const [dashboard, pets, reports, media, matching] = await Promise.all([
        core.api("/api/bff/dashboard", { token: state.session.token }),
        core.api("/api/pets", { token: state.session.token }),
        core.api("/api/reports", { token: state.session.token }),
        core.api("/api/media", { token: state.session.token }),
        core.api("/api/matching", { token: state.session.token })
      ]);

      state.dashboard = dashboard;
      state.pets = pets;
      state.reports = reports;
      state.media = media;
      state.matching = matching;

      renderKpis();
      renderPetOptions();
      renderLists();
      renderReportsOnMap();
      setStatus("Datos actualizados correctamente.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function onCreatePet(event) {
    event.preventDefault();

    const payload = {
      chipNumber: els.petChip.value.trim(),
      name: els.petName.value.trim(),
      species: els.petSpecies.value,
      breed: els.petBreed.value.trim(),
      color: els.petColor.value.trim(),
      size: els.petSize.value.trim(),
      ownerId: state.session.user.id || "citizen"
    };

    if (!payload.chipNumber || !payload.name) {
      return setStatus("Mascota requiere chip y nombre.", true);
    }

    try {
      await core.api("/api/pets", { method: "POST", token: state.session.token, body: payload });
      els.petForm.reset();
      els.petSpecies.value = "DOG";
      await refreshAll();
      setStatus("Mascota registrada.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function onCreateReport(event) {
    event.preventDefault();

    const payload = {
      petId: els.reportPetId.value,
      type: els.reportType.value,
      status: "OPEN",
      healthStatus: els.reportHealth.value.trim(),
      commune: els.reportCommune.value.trim(),
      latitude: Number(els.reportLat.value),
      longitude: Number(els.reportLng.value),
      description: els.reportDescription.value.trim(),
      createdBy: state.session.user.id || "citizen"
    };

    if (!payload.petId || Number.isNaN(payload.latitude) || Number.isNaN(payload.longitude)) {
      return setStatus("Reporte requiere mascota, latitud y longitud validas.", true);
    }

    try {
      await core.api("/api/reports", { method: "POST", token: state.session.token, body: payload });
      els.reportForm.reset();
      await refreshAll();
      setStatus("Reporte publicado.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  async function onCreateMedia(event) {
    event.preventDefault();

    const payload = {
      petId: els.mediaPetId.value,
      url: els.mediaUrl.value.trim(),
      takenAt: new Date().toISOString(),
      tags: els.mediaTags.value
        .split(",")
        .map((tag) => tag.trim())
        .filter(Boolean)
    };

    if (!payload.petId || !payload.url) {
      return setStatus("Media requiere mascota y URL.", true);
    }

    try {
      await core.api("/api/media", { method: "POST", token: state.session.token, body: payload });
      els.mediaForm.reset();
      await refreshAll();
      setStatus("Evidencia guardada.");
    } catch (error) {
      setStatus(error.message, true);
    }
  }

  function onLogout() {
    core.clearSession("citizen");
    window.location.href = "./index.html";
  }

  function renderKpis() {
    const d = state.dashboard || {};
    const cards = [
      ["Mascotas", d.totalPets || state.pets.length || 0, "Catalogo biometrico"],
      ["Reportes", d.totalReports || state.reports.length || 0, "Perdidas y hallazgos"],
      ["Coincidencias IA", state.matching.length || 0, "Cruce entre perdidas y hallazgos"],
      ["Media", d.totalPhotos || state.media.length || 0, "Evidencia fotografica"]
    ];

    els.citizenKpis.innerHTML = cards
      .map(
        (card) => `
          <article class="kpi-card">
            <span>${core.escapeHtml(card[0])}</span>
            <strong>${core.escapeHtml(String(card[1]))}</strong>
            <p class="activity-meta">${core.escapeHtml(card[2])}</p>
          </article>
        `
      )
      .join("");
  }

  function renderPetOptions() {
    const options = state.pets.length
      ? state.pets
          .map((pet) => {
            const label = `${pet.name || "Mascota"} - ${pet.chipNumber || "Sin chip"}`;
            return `<option value="${core.escapeHtml(pet.id)}">${core.escapeHtml(label)}</option>`;
          })
          .join("")
      : '<option value="">Primero registra una mascota</option>';

    els.reportPetId.innerHTML = options;
    els.mediaPetId.innerHTML = options;
  }

  function renderLists() {
    els.petsList.innerHTML = renderStack(
      state.pets.slice(0, 5).map((pet) => ({
        title: pet.name || "Mascota sin nombre",
        meta: `${pet.species || "N/A"} - ${pet.breed || "Sin raza"}`
      })),
      "Sin mascotas registradas aun."
    );

    els.reportsList.innerHTML = renderStack(
      state.reports.slice(0, 5).map((report) => ({
        title: `${report.type || "REPORTE"} - ${report.commune || "Sin comuna"}`,
        meta: core.truncate(report.description || "Sin descripcion", 90)
      })),
      "Sin reportes registrados aun."
    );

    els.matchingList.innerHTML = renderStack(
      state.matching.slice(0, 5).map((match) => ({
        title: `Match ${match.id || "-"} - Score ${match.score || "0"}`,
        meta: core.truncate(match.explanation || "Sin explicacion de coincidencia", 90)
      })),
      "Sin coincidencias IA registradas aun."
    );
  }

  function renderStack(items, emptyMessage) {
    if (!items.length) {
      return `<article class="stack-item"><strong>Sin datos</strong><p>${core.escapeHtml(emptyMessage)}</p></article>`;
    }

    return items
      .map(
        (item) => `
          <article class="stack-item">
            <strong>${core.escapeHtml(item.title)}</strong>
            <p>${core.escapeHtml(item.meta)}</p>
          </article>
        `
      )
      .join("");
  }

  function setStatus(message, isError) {
    els.citizenStatus.textContent = message;
    els.citizenStatus.style.color = isError ? "#b74f4f" : "";
  }
})();
