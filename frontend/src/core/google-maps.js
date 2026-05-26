(function () {
  const MAP_DARK_STYLES = [
    { elementType: "geometry", stylers: [{ color: "#1d2c4d" }] },
    { elementType: "labels.text.fill", stylers: [{ color: "#8ec3b9" }] },
    { elementType: "labels.text.stroke", stylers: [{ color: "#1a3646" }] },
    { featureType: "road", elementType: "geometry", stylers: [{ color: "#304a7d" }] },
    { featureType: "road", elementType: "geometry.stroke", stylers: [{ color: "#255763" }] },
    { featureType: "water", elementType: "geometry", stylers: [{ color: "#0e1626" }] },
    { featureType: "poi", stylers: [{ visibility: "off" }] }
  ];

  let loadPromise = null;

  function getApiKey() {
    if (window.SANOS_CORE && typeof window.SANOS_CORE.loadSettings === "function") {
      const settings = window.SANOS_CORE.loadSettings();
      if (settings.mapsApiKey) return settings.mapsApiKey.trim();
    }
    const cfg = window.SANOS_CONFIG || {};
    return String(cfg.googleMapsApiKey || "").trim();
  }

  function isDarkTheme() {
    return document.documentElement.getAttribute("data-theme") === "dark";
  }

  function loadGoogleMaps() {
    if (window.google && window.google.maps) {
      return Promise.resolve(window.google.maps);
    }
    const key = getApiKey();
    if (!key) {
      return Promise.reject(
        new Error("Configura googleMapsApiKey en frontend/src/core/config.js (Maps JavaScript API).")
      );
    }
    if (loadPromise) return loadPromise;
    loadPromise = new Promise((resolve, reject) => {
      const id = "sanos-google-maps-script";
      const existing = document.getElementById(id);
      if (existing) {
        existing.addEventListener("load", () => resolve(window.google.maps));
        existing.addEventListener("error", () => reject(new Error("No se pudo cargar Google Maps")));
        return;
      }
      const script = document.createElement("script");
      script.id = id;
      script.async = true;
      script.defer = true;
      script.src = `https://maps.googleapis.com/maps/api/js?key=${encodeURIComponent(key)}&v=weekly&language=es&region=CL`;
      script.onload = () => {
        if (window.google && window.google.maps) resolve(window.google.maps);
        else reject(new Error("Google Maps no disponible"));
      };
      script.onerror = () => reject(new Error("Error al cargar el script de Google Maps"));
      document.head.appendChild(script);
    });
    return loadPromise;
  }

  function showPlaceholder(container, message) {
    container.innerHTML = "";
    const box = document.createElement("div");
    box.className = "map-canvas__placeholder";
    box.innerHTML = `<p><strong>Google Maps</strong></p><p>${message}</p>`;
    container.appendChild(box);
  }

  function circleSymbol(color, scale) {
    return {
      path: google.maps.SymbolPath.CIRCLE,
      scale: scale || 8,
      fillColor: color,
      fillOpacity: 0.85,
      strokeColor: color,
      strokeWeight: 2
    };
  }

  /**
   * @returns {Promise<{
   *   map: google.maps.Map,
   *   infoWindow: google.maps.InfoWindow,
   *   markers: google.maps.Marker[],
   *   circles: google.maps.Circle[],
   *   pickMarker: google.maps.Marker|null,
   *   clearMarkers: Function,
   *   clearCircles: Function,
   *   addMarker: Function,
   *   addCircle: Function,
   *   setPickMarker: Function,
   *   fitPoints: Function,
   *   invalidateSize: Function,
   *   destroy: Function
   * }>}
   */
  async function createMap(container, options) {
    if (!container) throw new Error("Contenedor de mapa no encontrado");
    const center = options.center || { lat: -33.4489, lng: -70.6693 };
    const zoom = options.zoom == null ? 12 : options.zoom;

    let maps;
    try {
      maps = await loadGoogleMaps();
    } catch (err) {
      showPlaceholder(container, err.message || "Mapa no disponible");
      throw err;
    }

    container.innerHTML = "";
    const map = new maps.Map(container, {
      center: { lat: center.lat, lng: center.lng },
      zoom,
      mapTypeControl: true,
      streetViewControl: false,
      fullscreenControl: true,
      styles: isDarkTheme() ? MAP_DARK_STYLES : undefined
    });

    const infoWindow = new maps.InfoWindow({ maxWidth: 260, disableAutoPan: false });
    const state = {
      map,
      infoWindow,
      markers: [],
      circles: [],
      pickMarker: null,
      clickListener: null
    };

    if (typeof options.onClick === "function") {
      state.clickListener = map.addListener("click", (event) => {
        options.onClick({
          lat: event.latLng.lat(),
          lng: event.latLng.lng()
        });
      });
    }

    state.clearMarkers = function clearMarkers() {
      state.markers.forEach((m) => m.setMap(null));
      state.markers = [];
    };

    state.clearCircles = function clearCircles() {
      state.circles.forEach((c) => c.setMap(null));
      state.circles = [];
    };

    state.addMarker = function addMarker(lat, lng, opts) {
      const marker = new maps.Marker({
        map,
        position: { lat, lng },
        icon: circleSymbol(opts.color || "#3d8f73", opts.scale || 8),
        title: opts.title || "",
        zIndex: opts.zIndex || 1
      });
      if (opts.popupHtml) {
        marker.addListener("click", () => {
          state.infoWindow.setContent(opts.popupHtml);
          state.infoWindow.open({ map, anchor: marker });
        });
      }
      state.markers.push(marker);
      return marker;
    };

    state.addCircle = function addCircle(lat, lng, opts) {
      const circle = new maps.Circle({
        map,
        center: { lat, lng },
        radius: opts.radius || 800,
        strokeColor: opts.color || "#3d8f73",
        strokeOpacity: 0.9,
        strokeWeight: 2,
        fillColor: opts.color || "#3d8f73",
        fillOpacity: opts.fillOpacity == null ? 0.25 : opts.fillOpacity,
        clickable: true,
        zIndex: opts.zIndex || 0
      });
      if (opts.popupHtml) {
        circle.addListener("click", () => {
          state.infoWindow.setContent(opts.popupHtml);
          state.infoWindow.setPosition({ lat, lng });
          state.infoWindow.open(map);
        });
      }
      state.circles.push(circle);
      return circle;
    };

    state.setPickMarker = function setPickMarker(lat, lng, opts) {
      const color = opts.color || "#f59e0b";
      if (state.pickMarker) {
        state.pickMarker.setPosition({ lat, lng });
      } else {
        state.pickMarker = new maps.Marker({
          map,
          position: { lat, lng },
          icon: circleSymbol(color, opts.scale || 10),
          zIndex: 1000
        });
      }
      if (opts.popupHtml) {
        state.infoWindow.setContent(opts.popupHtml);
        state.infoWindow.open({ map, anchor: state.pickMarker });
      }
      if (opts.pan !== false) {
        map.panTo({ lat, lng });
        if ((map.getZoom() || 0) < 14) map.setZoom(14);
      }
      return state.pickMarker;
    };

    state.fitPoints = function fitPoints(points, padding) {
      if (!points || !points.length) return;
      const bounds = new maps.LatLngBounds();
      points.forEach((p) => {
        const lat = Array.isArray(p) ? p[0] : p.lat;
        const lng = Array.isArray(p) ? p[1] : p.lng;
        if (Number.isFinite(lat) && Number.isFinite(lng)) {
          bounds.extend({ lat, lng });
        }
      });
      map.fitBounds(bounds, padding || 40);
    };

    state.invalidateSize = function invalidateSize() {
      maps.event.trigger(map, "resize");
    };

    state.destroy = function destroy() {
      if (state.clickListener) {
        maps.event.removeListener(state.clickListener);
        state.clickListener = null;
      }
      state.clearMarkers();
      state.clearCircles();
      if (state.pickMarker) {
        state.pickMarker.setMap(null);
        state.pickMarker = null;
      }
      state.infoWindow.close();
      container.innerHTML = "";
    };

    requestAnimationFrame(() => state.invalidateSize());
    return state;
  }

  window.SANOS_MAPS = {
    loadGoogleMaps,
    createMap,
    getApiKey,
    isDarkTheme
  };
})();
