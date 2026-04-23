package com.sanos.petcatalogservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "PetDto", description = "Contrato API mascota: agrega datos de mascotas + caracteristicas_fisicas + vinculos_mascotas (db_pets).")
public record PetDto(
        @Schema(description = "PK mascotas.id_mascota", example = "1", accessMode = Schema.AccessMode.READ_ONLY) Long id,
        @Schema(description = "Nombre mascota", example = "Milo", requiredMode = Schema.RequiredMode.REQUIRED) String name,
        @Schema(description = "Especie", example = "DOG", allowableValues = {"DOG", "CAT"}) String species,
        @Schema(description = "Raza (caracteristicas_fisicas.raza)") String breed,
        @Schema(description = "Color (caracteristicas_fisicas.color_principal)") String color,
        @Schema(description = "Tamano (caracteristicas_fisicas.tamano)") String size,
        @Schema(description = "Chip unico (mascotas.numero_chip)", requiredMode = Schema.RequiredMode.REQUIRED) String chipNumber,
        @Schema(description = "FK usuario dueno (vinculos_mascotas.id_usuario)", example = "1") Long ownerId,
        @Schema(description = "fecha_registro ISO", accessMode = Schema.AccessMode.READ_ONLY) String createdAt
) {}
