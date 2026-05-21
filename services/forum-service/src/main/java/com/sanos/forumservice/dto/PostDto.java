package com.sanos.forumservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PostDto", description = "Mensaje en un hilo (tabla mensajes_foro)")
public record PostDto(
        @Schema(description = "PK mensajes_foro.id_mensaje", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "FK hilos_foro.id_hilo") Long threadId,
        @Schema(description = "Contenido") String content,
        @Schema(description = "id_usuario") Long authorId,
        @Schema(description = "Nombre autor") String authorName,
        @Schema(description = "fecha_creacion ISO") String createdAt
) {}
