package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginRequest", description = "Credenciales POST /api/iam/login.")
public record LoginRequest(
        @Schema(description = "Correo registrado", example = "ciudadano@sanosysalvos.cl", requiredMode = Schema.RequiredMode.REQUIRED) String email,
        @Schema(description = "Password", requiredMode = Schema.RequiredMode.REQUIRED) String password
) {}
