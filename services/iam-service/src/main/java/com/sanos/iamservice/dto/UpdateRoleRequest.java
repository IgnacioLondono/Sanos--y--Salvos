package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateRoleRequest", description = "Nuevo rol IAM (solo ADMIN autenticado).")
public record UpdateRoleRequest(
        @Schema(description = "ADMIN o CITIZEN", example = "CITIZEN", requiredMode = Schema.RequiredMode.REQUIRED) String role
) {}
