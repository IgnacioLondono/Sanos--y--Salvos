package com.sanos.matchingservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "MatchDto", description = "Resultado matching IA (tablas coincidencias_ia + desglose_similitud, db_matching).")
public record MatchDto(
        @Schema(description = "PK id_match", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "FK reporte perdida", example = "1") Long lostReportId,
        @Schema(description = "FK reporte hallazgo", example = "2") Long foundReportId,
        @Schema(description = "score_total 0..1", example = "0.82") Float score,
        @Schema(description = "Texto explicativo del motor") String explanation,
        @Schema(description = "creado_en ISO", accessMode = Schema.AccessMode.READ_ONLY) String createdAt
) {}
