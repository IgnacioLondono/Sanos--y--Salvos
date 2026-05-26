package com.sanos.capacityservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CapacitySummaryDto", description = "Totales agregados de equipos y voluntarios.")
public record CapacitySummaryDto(
        @Schema(description = "Cantidad de equipos", example = "3") int teams,
        @Schema(description = "Suma de voluntarios", example = "24") int volunteers,
        @Schema(description = "Suma de horas disponibles", example = "120") int hoursAvailable
) {}
