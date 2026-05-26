package com.sanos.capacityservice.controller;

import com.sanos.capacityservice.dto.CapacityDto;
import com.sanos.capacityservice.model.EquipoColaboracion;
import com.sanos.capacityservice.repository.EquipoColaboracionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CapacityControllerTest {

    @Mock
    private EquipoColaboracionRepository repo;

    private CapacityController controller;

    @BeforeEach
    void setUp() {
        controller = new CapacityController(repo);
    }

    @Test
    void create_returnsCreatedTeam() {
        CapacityDto req = new CapacityDto(
                null, "Brigada Norte", "ONG Rescate", "Providencia",
                12, 40, "2026-05-24T10:00:00", null
        );
        when(repo.save(any(EquipoColaboracion.class))).thenAnswer(inv -> {
            EquipoColaboracion e = inv.getArgument(0);
            e.setIdEquipo(5L);
            return e;
        });

        ResponseEntity<CapacityDto> response = controller.create(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(5L, response.getBody().id());
        assertEquals("Providencia", response.getBody().zone());
    }

    @Test
    void byZone_filtersIgnoreCase() {
        EquipoColaboracion e = team(8L, "Nunoa", 3, 10);
        when(repo.findByZonaOperacionIgnoreCase("Nunoa")).thenReturn(List.of(e));

        List<CapacityDto> result = controller.byZone("Nunoa");

        assertEquals(1, result.size());
        assertEquals(8L, result.get(0).id());
    }

    @Test
    void summary_calculatesTotals() {
        when(repo.findAll()).thenReturn(List.of(
                team(1L, "Santiago", 10, 20),
                team(2L, "Santiago", null, 5),
                team(3L, "Providencia", 7, null)
        ));

        var summary = controller.summary();

        assertEquals(3, summary.teams());
        assertEquals(17, summary.volunteers());
        assertEquals(25, summary.hoursAvailable());
    }

    @Test
    void health_returnsUpStatus() {
        Map<String, String> result = controller.health();
        assertEquals("UP", result.get("status"));
        assertEquals("capacity-service", result.get("service"));
    }

    private EquipoColaboracion team(Long id, String zone, Integer volunteers, Integer hours) {
        EquipoColaboracion e = new EquipoColaboracion();
        e.setIdEquipo(id);
        e.setNombreEquipo("Equipo " + id);
        e.setOrganizacion("Org");
        e.setZonaOperacion(zone);
        e.setVoluntarios(volunteers);
        e.setHorasDisponibles(hours);
        e.setDisponibleDesde(LocalDateTime.now());
        e.setFechaCreacion(LocalDateTime.now());
        return e;
    }
}
