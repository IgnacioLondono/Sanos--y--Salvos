package com.sanos.mediaservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

@Schema(name = "MediaDto", description = "Evidencia fotografica (tabla fotografias_mascotas, db_media).")
public record MediaDto(
        @Schema(description = "PK id_foto", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "FK mascota", example = "1") Long petId,
        @Schema(description = "FK reporte opcional", example = "1") Long reportId,
        @Schema(description = "URL almacenamiento (S3/CDN simulado)", example = "https://cdn.example/photo.jpg", requiredMode = Schema.RequiredMode.REQUIRED) String url,
        @Schema(description = "Etiquetas como lista (persistidas separadas por coma)") List<String> tags,
        @Schema(description = "fecha_captura ISO-8601", example = "2026-04-23T12:00:00") String takenAt
) {}
