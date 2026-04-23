package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "LoginResponse", description = "Respuesta exitosa de login: JWT + datos minimos de sesion.")
public record LoginResponse(
        @Schema(description = "JWT Bearer", example = "eyJhbGciOiJIUzI1NiIs...") String token,
        @Schema(description = "id_usuario", example = "1") Long id,
        @Schema(description = "Correo", example = "user@mail.cl") String email,
        @Schema(description = "Nombre para mostrar") String displayName,
        @Schema(description = "Rol", example = "ADMIN") String role
) {}
