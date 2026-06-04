package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateStatusRequest", description = "Cambio de estado de un reporte (PATCH /api/reports/{id}/status).")
public record UpdateStatusRequest(
        @Schema(
                description = "Nuevo estado del reporte",
                example = "OPEN",
                allowableValues = {"OPEN", "CLOSED", "ABIERTO", "CERRADO", "RESOLVED", "RESUELTO"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String status,
        @Schema(
                description = "Tipo opcional (LOST | FOUND). Permite volver a marcar perdida tras encontrada.",
                example = "LOST",
                allowableValues = {"LOST", "FOUND", "PERDIDA", "ENCONTRADA"})
        String type
) {}
