package com.sanos.reportsservice.controller;

import com.sanos.reportsservice.dto.ReportDto;
import com.sanos.reportsservice.dto.UpdateStatusRequest;
import com.sanos.reportsservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
@Tag(name = "Reportes", description = "Eventos perdida/hallazgo. Tablas: reportes_eventos, detalles_reporte (db_reports).")
public class ReportController {

    private final ReportService service;

    public ReportController(ReportService service) {
        this.service = service;
    }

    @Operation(summary = "Listar reportes")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ReportDto.class)))
    @GetMapping
    public List<ReportDto> listAll() { return service.listAll(); }

    @Operation(summary = "Crear reporte", description = "Persiste reporte y detalle asociado.")
    @ApiResponse(responseCode = "201", content = @Content(schema = @Schema(implementation = ReportDto.class)))
    @PostMapping
    public ResponseEntity<ReportDto> create(@RequestBody ReportDto req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(service.create(req));
    }

    @Operation(summary = "Reporte por ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ReportDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/{id}")
    public ResponseEntity<ReportDto> byId(
            @Parameter(description = "id_reporte", required = true) @PathVariable Long id) {
        return service.findById(id).map(ResponseEntity::ok).orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Reportes por mascota", description = "Filtra por id_mascota.")
    @GetMapping("/pet/{petId}")
    public List<ReportDto> byPet(
            @Parameter(description = "id_mascota", required = true) @PathVariable Long petId) {
        return service.findByPet(petId);
    }

    @Operation(summary = "Reportes por usuario creador")
    @GetMapping("/user/{userId}")
    public List<ReportDto> byUser(
            @Parameter(description = "id_usuario creador", required = true) @PathVariable Long userId) {
        return service.findByUser(userId);
    }

    @Operation(summary = "Reportes por estado", description = "Filtra por campo estado (ej. OPEN).")
    @GetMapping("/status/{status}")
    public List<ReportDto> byStatus(
            @Parameter(description = "estado", example = "OPEN", required = true) @PathVariable String status) {
        return service.findByStatus(status);
    }

    @Operation(summary = "Eliminar reporte", description = "Elimina el reporte y su detalle asociado.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Eliminado"),
            @ApiResponse(responseCode = "404", description = "Reporte no existe")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @Parameter(description = "id_reporte", required = true) @PathVariable Long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }

    @Operation(
            summary = "Actualizar estado",
            description = "PATCH con status (OPEN, RESOLVED, CLOSED, …). Campo opcional type (LOST|FOUND) "
                    + "permite volver a marcar perdida. Si status es RESOLVED/CLOSED y el reporte era LOST, type pasa a FOUND.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = ReportDto.class))),
            @ApiResponse(responseCode = "404", description = "Reporte no existe")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<ReportDto> updateStatus(
            @Parameter(description = "id_reporte", required = true) @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = UpdateStatusRequest.class)))
            @RequestBody UpdateStatusRequest payload) {
        String newStatus = payload.status() != null && !payload.status().isBlank()
                ? payload.status()
                : "ABIERTO";
        String newType = payload.type();
        return service.updateStatus(id, newStatus, newType)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Salud del servicio")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "reports-service");
    }
}
