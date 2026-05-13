package com.sanos.geointelligenceservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ZoneDto", description = "Zona de incidencia + punto (db_geo: zonas_incidencia, coordenadas_reporte).")
public record ZoneDto(
        @Schema(description = "PK id_zona", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "nombre_comuna", example = "Providencia", requiredMode = Schema.RequiredMode.REQUIRED) String commune,
        @Schema(description = "nivel_riesgo", example = "MEDIUM") String riskLevel,
        @Schema(description = "Latitud centro o punto") BigDecimal latitude,
        @Schema(description = "Longitud") BigDecimal longitude,
        @Schema(description = "FK reporte vinculado opcional", example = "1") Long reportId
) {}
