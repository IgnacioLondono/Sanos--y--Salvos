package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateContactMessageRequest", description = "Enviar mensaje en chat de contacto.")
public record CreateContactMessageRequest(
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) Long authorUserId,
        @Schema(requiredMode = Schema.RequiredMode.REQUIRED) String content
) {}
