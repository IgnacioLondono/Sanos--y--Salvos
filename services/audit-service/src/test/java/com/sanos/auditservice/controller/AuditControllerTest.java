package com.sanos.auditservice.controller;

import com.sanos.auditservice.dto.AuditDto;
import com.sanos.auditservice.model.LogAuditoria;
import com.sanos.auditservice.repository.LogAuditoriaRepository;
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
class AuditControllerTest {

    @Mock
    private LogAuditoriaRepository repo;

    private AuditController controller;

    @BeforeEach
    void setUp() {
        controller = new AuditController(repo);
    }

    @Test
    void create_returnsCreatedAudit() {
        AuditDto req = new AuditDto(null, "REPORTE", "CREATE", "admin", "{\"x\":1}", null);
        when(repo.save(any(LogAuditoria.class))).thenAnswer(inv -> {
            LogAuditoria l = inv.getArgument(0);
            l.setIdLog(77L);
            l.setCreadoEn(LocalDateTime.now());
            return l;
        });

        ResponseEntity<AuditDto> response = controller.create(req);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(77L, response.getBody().id());
        assertEquals("REPORTE", response.getBody().entity());
    }

    @Test
    void list_andFilters_mapToDto() {
        LogAuditoria l = new LogAuditoria();
        l.setIdLog(1L);
        l.setEntidad("MASCOTA");
        l.setOperacion("UPDATE");
        l.setActor("tester");
        l.setCambiosJson("{}");
        l.setCreadoEn(LocalDateTime.now());
        when(repo.findAll()).thenReturn(List.of(l));
        when(repo.findByEntidadIgnoreCase("MASCOTA")).thenReturn(List.of(l));
        when(repo.findByActorIgnoreCase("tester")).thenReturn(List.of(l));

        assertEquals(1, controller.list().size());
        assertEquals(1, controller.byEntity("MASCOTA").size());
        assertEquals(1, controller.byActor("tester").size());
    }

    @Test
    void health_returnsUp() {
        Map<String, String> result = controller.health();
        assertEquals("UP", result.get("status"));
        assertEquals("audit-service", result.get("service"));
    }
}
