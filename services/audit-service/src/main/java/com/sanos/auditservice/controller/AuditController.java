package com.sanos.auditservice.controller;

import com.sanos.auditservice.dto.AuditDto;
import com.sanos.auditservice.model.LogAuditoria;
import com.sanos.auditservice.repository.LogAuditoriaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/audit")
@CrossOrigin(origins = "*")
public class AuditController {

    private final LogAuditoriaRepository repo;

    public AuditController(LogAuditoriaRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<AuditDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

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

    @GetMapping("/entity/{entity}")
    public List<AuditDto> byEntity(@PathVariable String entity) {
        return repo.findByEntidadIgnoreCase(entity).stream().map(this::toDto).toList();
    }

    @GetMapping("/actor/{actor}")
    public List<AuditDto> byActor(@PathVariable String actor) {
        return repo.findByActorIgnoreCase(actor).stream().map(this::toDto).toList();
    }

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
