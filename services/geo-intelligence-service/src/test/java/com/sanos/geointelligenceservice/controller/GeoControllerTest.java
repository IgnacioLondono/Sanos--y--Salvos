package com.sanos.geointelligenceservice.controller;

import com.sanos.geointelligenceservice.dto.ZoneDto;
import com.sanos.geointelligenceservice.model.CoordenadaReporte;
import com.sanos.geointelligenceservice.model.ZonaIncidencia;
import com.sanos.geointelligenceservice.repository.CoordenadaReporteRepository;
import com.sanos.geointelligenceservice.repository.ZonaIncidenciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GeoControllerTest {

    @Mock
    private ZonaIncidenciaRepository zoneRepo;
    @Mock
    private CoordenadaReporteRepository coordRepo;

    private GeoController controller;

    @BeforeEach
    void setUp() {
        controller = new GeoController(zoneRepo, coordRepo);
    }

    @Test
    void create_withCoordinates_savesCoordinateAndZone() {
        ZoneDto req = new ZoneDto(
                null, "Santiago", "ALTO",
                new BigDecimal("-33.45"), new BigDecimal("-70.66"), 9L
        );
        when(coordRepo.save(any(CoordenadaReporte.class))).thenAnswer(inv -> {
            CoordenadaReporte c = inv.getArgument(0);
            c.setIdCoordenada(88L);
            return c;
        });
        when(zoneRepo.save(any(ZonaIncidencia.class))).thenAnswer(inv -> {
            ZonaIncidencia z = inv.getArgument(0);
            z.setIdZona(44L);
            return z;
        });

        ResponseEntity<ZoneDto> response = controller.create(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(44L, response.getBody().id());
        assertEquals("Santiago", response.getBody().commune());
    }

    @Test
    void riskSummary_returnsDefaultWhenNoData() {
        when(zoneRepo.findAll()).thenReturn(List.of());

        Map<String, Long> summary = controller.riskSummary();

        assertEquals(0L, summary.get("INDEFINIDO"));
    }

    @Test
    void health_returnsUpStatus() {
        Map<String, String> result = controller.health();
        assertEquals("UP", result.get("status"));
        assertEquals("geo-intelligence-service", result.get("service"));
    }
}
