package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;

@Schema(name = "ReportDto", description = "Contrato API reporte: mapea reportes_eventos + detalles_reporte (db_reports).")
public record ReportDto(
        @Schema(description = "PK reportes_eventos.id_reporte", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "FK mascotas.id_mascota", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long petId,
        @Schema(description = "Usuario creador (id logico IAM)", example = "1") Long createdBy,
        @Schema(description = "LOST | FOUND", example = "LOST", allowableValues = {"LOST", "FOUND"}) String type,
        @Schema(description = "Estado workflow", example = "OPEN") String status,
        @Schema(description = "Comuna del hecho") String commune,
        @Schema(description = "Texto libre") String description,
        @Schema(description = "Condicion salud observada") String healthStatus,
        @Schema(description = "Latitud WGS84", example = "-33.448900") BigDecimal latitude,
        @Schema(description = "Longitud WGS84", example = "-70.669300") BigDecimal longitude,
        @Schema(description = "fecha_creacion ISO", accessMode = Schema.AccessMode.READ_ONLY) String createdAt
) {}
