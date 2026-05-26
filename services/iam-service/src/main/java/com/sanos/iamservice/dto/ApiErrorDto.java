package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "ApiError", description = "Respuesta de error JSON del servicio.")
public record ApiErrorDto(
        @Schema(description = "Mensaje legible", example = "Token requerido")
        String error
) {}
