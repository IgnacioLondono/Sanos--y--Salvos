package com.sanos.auditservice.controller;

import com.sanos.auditservice.dto.AuditDto;
import com.sanos.auditservice.model.LogAuditoria;
import com.sanos.auditservice.repository.LogAuditoriaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
@Tag(name = "Auditoria", description = "Logs y notificaciones. Tablas: log_auditoria, notificaciones_sistema (db_audit).")
public class AuditController {

    private final LogAuditoriaRepository repo;

    public AuditController(LogAuditoriaRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Listar logs")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AuditDto.class)))
    @GetMapping
    public List<AuditDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Registrar evento de auditoria", description = "Inserta log_auditoria.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = AuditDto.class)))
    @PostMapping
    public ResponseEntity<AuditDto> create(@RequestBody AuditDto req) {
        LogAuditoria l = new LogAuditoria();
        l.setEntidad(req.entity());
        l.setOperacion(req.operation());
        l.setActor(req.actor());
        l.setTablaAfectada(req.entity());
        l.setAccionRealizada(req.operation());
        l.setCambiosJson(req.changes());
        l.setCreadoEn(LocalDateTime.now());
        l = repo.save(l);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(l));
    }

    @Operation(summary = "Logs por entidad")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AuditDto.class)))
    @GetMapping("/entity/{entity}")
    public List<AuditDto> byEntity(
            @Parameter(description = "Nombre entidad", required = true) @PathVariable String entity) {
        return repo.findByEntidadIgnoreCase(entity).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Logs por actor")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = AuditDto.class)))
    @GetMapping("/actor/{actor}")
    public List<AuditDto> byActor(
            @Parameter(description = "Actor", required = true) @PathVariable String actor) {
        return repo.findByActorIgnoreCase(actor).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Salud del servicio")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "audit-service");
    }

    private AuditDto toDto(LogAuditoria l) {
        return new AuditDto(
                l.getIdLog(),
                l.getEntidad() == null ? l.getTablaAfectada() : l.getEntidad(),
                l.getOperacion() == null ? l.getAccionRealizada() : l.getOperacion(),
                l.getActor(),
                l.getCambiosJson(),
                l.getCreadoEn() != null ? l.getCreadoEn().toString() : null
        );
    }
}
