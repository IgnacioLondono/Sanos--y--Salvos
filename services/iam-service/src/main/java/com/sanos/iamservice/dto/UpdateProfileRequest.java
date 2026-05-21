package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UpdateProfileRequest", description = "Actualizacion de perfil del usuario autenticado.")
public record UpdateProfileRequest(
        @Schema(description = "Nombre completo") String fullName,
        @Schema(description = "Comuna") String commune,
        @Schema(description = "Direccion") String address,
        @Schema(description = "Telefono") String phone,
        @Schema(description = "Contacto emergencia") String emergencyContactName,
        @Schema(description = "Telefono emergencia") String emergencyContactPhone
) {}
