package com.sanos.capacityservice.controller;

import com.sanos.capacityservice.dto.CapacityDto;
import com.sanos.capacityservice.model.EquipoColaboracion;
import com.sanos.capacityservice.repository.EquipoColaboracionRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/capacity")
@CrossOrigin(origins = "*")
public class CapacityController {

    private final EquipoColaboracionRepository repo;

    public CapacityController(EquipoColaboracionRepository repo) {
        this.repo = repo;
    }

    @GetMapping
    public List<CapacityDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @PostMapping
    public ResponseEntity<CapacityDto> create(@RequestBody CapacityDto req) {
        EquipoColaboracion e = new EquipoColaboracion();
        e.setNombreEquipo(req.name());
        e.setOrganizacion(req.organization());
        e.setZonaOperacion(req.zone());
        e.setVoluntarios(req.volunteers());
        e.setHorasDisponibles(req.hoursAvailable());
        e.setDisponibleDesde(req.availableFrom() != null ? parseDate(req.availableFrom()) : LocalDateTime.now());
        e.setFechaCreacion(LocalDateTime.now());
        e = repo.save(e);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(e));
    }

    @GetMapping("/zone/{zone}")
    public List<CapacityDto> byZone(@PathVariable String zone) {
        return repo.findByZonaOperacionIgnoreCase(zone).stream().map(this::toDto).toList();
    }

    @GetMapping("/summary")
    public Map<String, Object> summary() {
        List<EquipoColaboracion> equipos = repo.findAll();
        int totalVoluntarios = equipos.stream().mapToInt(e -> e.getVoluntarios() == null ? 0 : e.getVoluntarios()).sum();
        int totalHoras = equipos.stream().mapToInt(e -> e.getHorasDisponibles() == null ? 0 : e.getHorasDisponibles()).sum();
        return Map.of(
                "teams", equipos.size(),
                "volunteers", totalVoluntarios,
                "hoursAvailable", totalHoras
        );
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "capacity-service");
    }

    private CapacityDto toDto(EquipoColaboracion e) {
        return new CapacityDto(
                e.getIdEquipo(),
                e.getNombreEquipo(),
                e.getOrganizacion(),
                e.getZonaOperacion(),
                e.getVoluntarios(),
                e.getHorasDisponibles(),
                e.getDisponibleDesde() != null ? e.getDisponibleDesde().toString() : null,
                e.getFechaCreacion() != null ? e.getFechaCreacion().toString() : null
        );
    }

    private LocalDateTime parseDate(String iso) {
        try { return LocalDateTime.parse(iso.replace("Z", "")); }
        catch (Exception ex) { return LocalDateTime.now(); }
    }
}
