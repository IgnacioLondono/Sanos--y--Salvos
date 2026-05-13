package com.sanos.geointelligenceservice.config;

import com.sanos.geointelligenceservice.model.CoordenadaReporte;
import com.sanos.geointelligenceservice.model.ZonaIncidencia;
import com.sanos.geointelligenceservice.repository.CoordenadaReporteRepository;
import com.sanos.geointelligenceservice.repository.ZonaIncidenciaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ZonaIncidenciaRepository zoneRepo;
    private final CoordenadaReporteRepository coordRepo;

    public DataSeeder(ZonaIncidenciaRepository zoneRepo, CoordenadaReporteRepository coordRepo) {
        this.zoneRepo = zoneRepo;
        this.coordRepo = coordRepo;
    }

    @Override
    public void run(String... args) {
        if (zoneRepo.count() > 0) return;

        addZone("Providencia", "MEDIO", new BigDecimal("-33.4489"), new BigDecimal("-70.6193"), 1L);
        addZone("Nunoa",       "BAJO",  new BigDecimal("-33.4563"), new BigDecimal("-70.5975"), 2L);
        addZone("Maipu",       "ALTO",  new BigDecimal("-33.5110"), new BigDecimal("-70.7580"), 3L);
        addZone("Santiago",    "ALTO",  new BigDecimal("-33.4372"), new BigDecimal("-70.6506"), null);
        addZone("La Florida",  "MEDIO", new BigDecimal("-33.5221"), new BigDecimal("-70.5980"), null);
    }

    private void addZone(String comuna, String riesgo, BigDecimal lat, BigDecimal lng, Long idReporte) {
        CoordenadaReporte coord = new CoordenadaReporte();
        coord.setLatitud(lat);
        coord.setLongitud(lng);
        coord.setIdReporte(idReporte);
        coord = coordRepo.save(coord);

        ZonaIncidencia z = new ZonaIncidencia();
        z.setIdCoordenada(coord.getIdCoordenada());
        z.setNombreComuna(comuna);
        z.setNivelRiesgo(riesgo);
        z.setLatitud(lat);
        z.setLongitud(lng);
        z.setIdReporte(idReporte);
        zoneRepo.save(z);
    }
}
