package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContactConversationDto", description = "Conversacion de chat tras solicitud aceptada.")
public record ContactConversationDto(
        Long id,
        Long requestId,
        Long reportId,
        Long fromUserId,
        Long toUserId,
        String status,
        String createdAt,
        String closedAt,
        Long closedByUserId,
        String lastMessagePreview,
        int messageCount
) {}
