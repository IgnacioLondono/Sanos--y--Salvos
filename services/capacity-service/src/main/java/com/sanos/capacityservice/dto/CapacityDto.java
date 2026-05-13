package com.sanos.capacityservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CapacityDto", description = "Equipo de colaboracion (tabla equipos_colaboracion, db_capacity). Asignaciones en asignacion_capacidad.")
public record CapacityDto(
        @Schema(description = "PK id_equipo", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "nombre_equipo", example = "Brigada Norte", requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(description = "organizacion", example = "ONG Rescate") String organization,
        @Schema(description = "zona_operacion", example = "Providencia") String zone,
        @Schema(description = "voluntarios", example = "12") Integer volunteers,
        @Schema(description = "horas_disponibles", example = "40") Integer hoursAvailable,
        @Schema(description = "disponible_desde ISO") String availableFrom,
        @Schema(description = "fecha_creacion", accessMode = Schema.AccessMode.READ_ONLY) String createdAt
) {}
