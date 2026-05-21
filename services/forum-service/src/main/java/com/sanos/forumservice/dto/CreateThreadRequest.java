package com.sanos.forumservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "CreateThreadRequest", description = "Cuerpo POST /api/forum/threads")
public record CreateThreadRequest(
        @Schema(description = "Titulo del hilo (min. 5 caracteres)", example = "Como marcar ubicacion en el mapa?", requiredMode = Schema.RequiredMode.REQUIRED)
        String title,
        @Schema(description = "Primer mensaje del hilo (min. 10 caracteres)", requiredMode = Schema.RequiredMode.REQUIRED)
        String content,
        @Schema(description = "Categoria", example = "AYUDA", allowableValues = {"AYUDA", "CONSEJOS", "GENERAL"})
        String category,
        @Schema(description = "id_usuario (IAM)", example = "1")
        Long authorId,
        @Schema(description = "Nombre visible del autor", example = "Ana Perez")
        String authorName
) {}
