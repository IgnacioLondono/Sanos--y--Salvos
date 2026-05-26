package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateStatusRequest", description = "Cambio de estado de un reporte (PATCH /api/reports/{id}/status).")
public record UpdateStatusRequest(
        @Schema(
                description = "Nuevo estado del reporte",
                example = "OPEN",
                allowableValues = {"OPEN", "CLOSED", "ABIERTO", "CERRADO"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String status
) {}
