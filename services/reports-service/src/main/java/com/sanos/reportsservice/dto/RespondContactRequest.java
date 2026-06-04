package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RespondContactRequest", description = "Aceptar o rechazar una solicitud recibida.")
public record RespondContactRequest(
        @Schema(description = "id_usuario que responde (debe ser el receptor)", requiredMode = Schema.RequiredMode.REQUIRED)
        Long responderUserId,
        @Schema(description = "ACCEPTED o REJECTED", allowableValues = {"ACCEPTED", "REJECTED"},
                requiredMode = Schema.RequiredMode.REQUIRED)
        String status
) {}
