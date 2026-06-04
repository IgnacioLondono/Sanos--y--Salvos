package com.sanos.reportsservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ContactMessageDto", description = "Mensaje en conversacion de contacto.")
public record ContactMessageDto(
        Long id,
        Long conversationId,
        Long authorUserId,
        String content,
        String createdAt
) {}
