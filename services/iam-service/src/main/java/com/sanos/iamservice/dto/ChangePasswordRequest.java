package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ChangePasswordRequest", description = "Cambio de contrasena autenticado (JWT).")
public record ChangePasswordRequest(
        @Schema(description = "Contrasena actual", requiredMode = Schema.RequiredMode.REQUIRED) String currentPassword,
        @Schema(description = "Nueva contrasena", requiredMode = Schema.RequiredMode.REQUIRED) String newPassword
) {}
