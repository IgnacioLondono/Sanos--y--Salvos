package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "AdminCreateRequest", description = "Alta de administrador por un ADMIN existente.")
public record AdminCreateRequest(
        @Schema(description = "Nombre completo", example = "Maria Admin", requiredMode = Schema.RequiredMode.REQUIRED)
        String fullName,
        @Schema(description = "RUT", example = "12345678-9", requiredMode = Schema.RequiredMode.REQUIRED)
        String rutDocument,
        @Schema(description = "Correo unico", example = "admin2@sanosysalvos.cl", requiredMode = Schema.RequiredMode.REQUIRED)
        String email,
        @Schema(description = "Contrasena inicial", requiredMode = Schema.RequiredMode.REQUIRED)
        String password,
        @Schema(description = "Comuna", example = "Santiago")
        String commune,
        @Schema(description = "Telefono", example = "+56 9 1234 5678")
        String phone
) {}
