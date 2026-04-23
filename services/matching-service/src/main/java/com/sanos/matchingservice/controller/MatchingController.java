package com.sanos.matchingservice.controller;

import com.sanos.matchingservice.dto.MatchDto;
import com.sanos.matchingservice.service.MatchingEngine;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/matching")
@CrossOrigin(origins = "*")
@Tag(name = "Matching IA", description = "Coincidencias perdida/hallazgo. Tablas: coincidencias_ia, desglose_similitud (db_matching).")
public class MatchingController {

    private final MatchingEngine engine;

    public MatchingController(MatchingEngine engine) {
        this.engine = engine;
    }

    @Operation(summary = "Listar coincidencias", description = "Todas las filas coincidencias_ia mapeadas a DTO.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MatchDto.class)))
    @GetMapping
    public List<MatchDto> list() { return engine.listAll(); }

    @Operation(summary = "Ejecutar matching completo", description = "Lee reportes vía HTTP, calcula y persiste matches.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MatchDto.class)))
    @PostMapping("/run")
    public List<MatchDto> run() { return engine.runFullMatching(); }

    @Operation(summary = "Crear match manual", description = "POST cuerpo MatchDto sin id para insertar a mano.")
    @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = MatchDto.class)))
    @PostMapping
    public MatchDto manualCreate(@RequestBody MatchDto req) { return engine.manualCreate(req); }

    @Operation(summary = "Matches por reporte", description = "Donde reporte aparece como perdida o hallazgo.")
    @GetMapping("/report/{reportId}")
    public List<MatchDto> byReport(
            @Parameter(description = "id_reporte", required = true) @PathVariable Long reportId) {
        return engine.byReport(reportId);
    }

    @Operation(summary = "Salud del servicio")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "matching-service");
    }
}
