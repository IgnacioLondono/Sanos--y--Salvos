package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContactRequestDto", description = "Solicitud de contacto vinculada a un reporte del mapa.")
public record ContactRequestDto(
        @Schema(description = "PK solicitud", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "id_reporte", example = "1") Long reportId,
        @Schema(description = "Usuario que envia la solicitud") Long fromUserId,
        @Schema(description = "Dueno del reporte / receptor") Long toUserId,
        @Schema(description = "Mensaje breve") String message,
        @Schema(description = "PENDING | ACCEPTED | REJECTED", example = "PENDING") String status,
        @Schema(description = "fecha_creacion ISO", accessMode = Schema.AccessMode.READ_ONLY) String createdAt,
        @Schema(description = "fecha_respuesta ISO", accessMode = Schema.AccessMode.READ_ONLY) String respondedAt,
        @Schema(description = "id_conversacion si fue aceptada", accessMode = Schema.AccessMode.READ_ONLY) Long conversationId
) {}
