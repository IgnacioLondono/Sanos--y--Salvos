package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CloseContactConversationRequest", description = "Cerrar chat (solo receptor de la solicitud).")
public record CloseContactConversationRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Long userId
) {}
