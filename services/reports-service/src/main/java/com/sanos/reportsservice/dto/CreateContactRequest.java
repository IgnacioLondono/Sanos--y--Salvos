package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateContactRequest", description = "Cuerpo para enviar solicitud de contacto desde el mapa.")
public record CreateContactRequest(
        @Schema(description = "id_reporte", example = "1", requiredMode = Schema.RequiredMode.REQUIRED) Long reportId,
        @Schema(description = "id_usuario emisor", example = "2", requiredMode = Schema.RequiredMode.REQUIRED) Long fromUserId,
        @Schema(description = "Mensaje (min. 10 caracteres)", requiredMode = Schema.RequiredMode.REQUIRED) String message
) {}
