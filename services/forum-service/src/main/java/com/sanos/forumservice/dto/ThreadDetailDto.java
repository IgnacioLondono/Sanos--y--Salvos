package com.sanos.forumservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;

@Schema(name = "ThreadDetailDto", description = "Hilo con lista de mensajes ordenados por fecha")
public record ThreadDetailDto(
        @Schema(description = "Metadatos del hilo") ThreadDto thread,
        @Schema(description = "Mensajes del hilo (incluye mensaje inicial)") List<PostDto> posts
) {}
