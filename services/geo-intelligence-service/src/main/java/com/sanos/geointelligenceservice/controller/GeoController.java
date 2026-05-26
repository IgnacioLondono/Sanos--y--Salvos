package com.sanos.geointelligenceservice.controller;

import com.sanos.geointelligenceservice.dto.ZoneDto;
import com.sanos.geointelligenceservice.model.CoordenadaReporte;
import com.sanos.geointelligenceservice.model.ZonaIncidencia;
import com.sanos.geointelligenceservice.repository.CoordenadaReporteRepository;
import com.sanos.geointelligenceservice.repository.ZonaIncidenciaRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/zones")
@CrossOrigin(origins = "*")
@Tag(name = "Geo (zonas)", description = "Zonas y coordenadas. Tablas: zonas_incidencia, coordenadas_reporte (db_geo).")
public class GeoController {

    private final ZonaIncidenciaRepository zoneRepo;
    private final CoordenadaReporteRepository coordRepo;

    public GeoController(ZonaIncidenciaRepository zoneRepo, CoordenadaReporteRepository coordRepo) {
        this.zoneRepo = zoneRepo;
        this.coordRepo = coordRepo;
    }

    @Operation(summary = "Listar zonas", description = "Todas las zonas como ZoneDto.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ZoneDto.class)))
    @GetMapping
    public List<ZoneDto> list() {
        return zoneRepo.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Crear zona", description = "Opcionalmente crea coordenada y enlaza id_coordenada.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ZoneDto.class)))
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

    @Operation(summary = "Zonas por comuna", description = "Busqueda case-insensitive por nombre_comuna.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ZoneDto.class)))
    @GetMapping("/commune/{commune}")
    public List<ZoneDto> byCommune(
            @Parameter(description = "Nombre comuna", example = "Providencia", required = true) @PathVariable String commune) {
        return zoneRepo.findByNombreComunaIgnoreCase(commune).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Resumen por nivel de riesgo", description = "Conteo agrupado por nivel_riesgo.")
    @ApiResponse(
            responseCode = "200",
            description = "Mapa nivel_riesgo → cantidad",
            content = @Content(schema = @Schema(
                    type = "object",
                    example = "{\"ALTO\":2,\"MEDIO\":5,\"BAJO\":1}",
                    additionalProperties = Schema.AdditionalPropertiesValue.TRUE)))
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

    @Operation(summary = "Listar coordenadas", description = "Entidad JPA expuesta (tabla coordenadas_reporte).")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CoordenadaReporte.class)))
    @GetMapping("/coordinates")
    public List<CoordenadaReporte> coordinates() {
        return coordRepo.findAll();
    }

    @Operation(summary = "Salud del servicio")
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
