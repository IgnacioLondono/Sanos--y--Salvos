package com.sanos.geointelligenceservice.controller;

import com.sanos.geointelligenceservice.dto.ZoneDto;
import com.sanos.geointelligenceservice.model.CoordenadaReporte;
import com.sanos.geointelligenceservice.model.ZonaIncidencia;
import com.sanos.geointelligenceservice.repository.CoordenadaReporteRepository;
import com.sanos.geointelligenceservice.repository.ZonaIncidenciaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = "*")
public class GeoController {

    private final ZonaIncidenciaRepository zoneRepo;
    private final CoordenadaReporteRepository coordRepo;

    public GeoController(ZonaIncidenciaRepository zoneRepo, CoordenadaReporteRepository coordRepo) {
        this.zoneRepo = zoneRepo;
        this.coordRepo = coordRepo;
    }

    @GetMapping
    public List<ZoneDto> list() {
        return zoneRepo.findAll().stream().map(this::toDto).toList();
    }

    @PostMapping
    public ResponseEntity<ZoneDto> create(@RequestBody ZoneDto req) {
        ZonaIncidencia z = new ZonaIncidencia();
        z.setNombreComuna(req.commune());
        z.setNivelRiesgo(req.riskLevel());
        z.setLatitud(req.latitude());
        z.setLongitud(req.longitude());
        z.setIdReporte(req.reportId());

        if (req.latitude() != null && req.longitude() != null) {
            CoordenadaReporte coord = new CoordenadaReporte();
            coord.setLatitud(req.latitude());
            coord.setLongitud(req.longitude());
            coord.setIdReporte(req.reportId());
            coord = coordRepo.save(coord);
            z.setIdCoordenada(coord.getIdCoordenada());
        }

        z = zoneRepo.save(z);
        return ResponseEntity.status(HttpStatus.CREATED).body(toDto(z));
    }

    @GetMapping("/commune/{commune}")
    public List<ZoneDto> byCommune(@PathVariable String commune) {
        return zoneRepo.findByNombreComunaIgnoreCase(commune).stream().map(this::toDto).toList();
    }

    @GetMapping("/risk-summary")
    public Map<String, Long> riskSummary() {
        Map<String, Long> grouped = zoneRepo.findAll().stream()
                .collect(Collectors.groupingBy(
                        z -> z.getNivelRiesgo() == null ? "INDEFINIDO" : z.getNivelRiesgo(),
                        Collectors.counting()));
        if (grouped.isEmpty()) {
            return Map.of("INDEFINIDO", 0L);
        }
        return grouped;
    }

    @GetMapping("/coordinates")
    public List<CoordenadaReporte> coordinates() {
        return coordRepo.findAll();
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "geo-intelligence-service");
    }

    private ZoneDto toDto(ZonaIncidencia z) {
        return new ZoneDto(
                z.getIdZona(),
                z.getNombreComuna(),
                z.getNivelRiesgo(),
                z.getLatitud(),
                z.getLongitud(),
                z.getIdReporte()
        );
    }
}
