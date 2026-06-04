package com.sanos.capacityservice.controller;

import com.sanos.capacityservice.dto.CapacityDto;
import com.sanos.capacityservice.dto.CapacitySummaryDto;
import com.sanos.capacityservice.model.EquipoColaboracion;
import com.sanos.capacityservice.repository.EquipoColaboracionRepository;
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
@RequestMapping("/api/capacity")
@CrossOrigin(origins = "*")
@Tag(name = "Capacity", description = "Equipos y capacidad. Tablas: equipos_colaboracion, asignacion_capacidad (db_capacity).")
public class CapacityController {

    private final EquipoColaboracionRepository repo;

    public CapacityController(EquipoColaboracionRepository repo) {
        this.repo = repo;
    }

    @Operation(summary = "Listar equipos")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CapacityDto.class)))
    @GetMapping
    public List<CapacityDto> list() {
        return repo.findAll().stream().map(this::toDto).toList();
    }

    @Operation(summary = "Crear equipo", description = "Inserta equipos_colaboracion.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = CapacityDto.class)))
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

    @Operation(summary = "Equipos por zona", description = "Filtra por zona_operacion (ignore case).")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CapacityDto.class)))
    @GetMapping("/zone/{zone}")
    public List<CapacityDto> byZone(
            @Parameter(description = "zona_operacion", required = true) @PathVariable String zone) {
        return repo.findByZonaOperacionIgnoreCase(zone).stream().map(this::toDto).toList();
    }

    @Operation(summary = "Resumen agregado", description = "Totales: equipos, voluntarios, horas.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = CapacitySummaryDto.class)))
    @GetMapping("/summary")
    public CapacitySummaryDto summary() {
        List<EquipoColaboracion> equipos = repo.findAll();
        int totalVoluntarios = equipos.stream().mapToInt(e -> e.getVoluntarios() == null ? 0 : e.getVoluntarios()).sum();
        int totalHoras = equipos.stream().mapToInt(e -> e.getHorasDisponibles() == null ? 0 : e.getHorasDisponibles()).sum();
        return new CapacitySummaryDto(equipos.size(), totalVoluntarios, totalHoras);
    }

    @Operation(summary = "Salud del servicio")
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
                com.sanos.capacityservice.util.ApiDateTimes.format(e.getDisponibleDesde()),
                com.sanos.capacityservice.util.ApiDateTimes.format(e.getFechaCreacion())
        );
    }

    private LocalDateTime parseDate(String iso) {
        try { return LocalDateTime.parse(iso.replace("Z", "")); }
        catch (Exception ex) { return LocalDateTime.now(); }
    }
}
