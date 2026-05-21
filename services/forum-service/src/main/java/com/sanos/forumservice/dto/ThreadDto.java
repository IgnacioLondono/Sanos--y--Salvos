package com.sanos.forumservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ThreadDto", description = "Resumen de hilo (tabla hilos_foro + conteo mensajes_foro)")
public record ThreadDto(
        @Schema(description = "PK hilos_foro.id_hilo", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "Titulo") String title,
        @Schema(description = "Categoria") String category,
        @Schema(description = "id_usuario creador") Long authorId,
        @Schema(description = "Nombre autor") String authorName,
        @Schema(description = "Extracto del primer mensaje") String preview,
        @Schema(description = "Cantidad de respuestas (sin contar mensaje inicial)") int replyCount,
        @Schema(description = "fecha_creacion ISO") String createdAt,
        @Schema(description = "fecha_actualizacion ISO") String updatedAt
) {}
