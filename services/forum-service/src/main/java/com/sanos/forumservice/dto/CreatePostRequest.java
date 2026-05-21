package com.sanos.forumservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreatePostRequest", description = "Cuerpo POST /api/forum/threads/{id}/posts")
public record CreatePostRequest(
        @Schema(description = "Texto de la respuesta", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "id_usuario (IAM)", example = "2")
        Long authorId,
        @Schema(description = "Nombre visible", example = "Pedro Soto")
        String authorName
) {}
