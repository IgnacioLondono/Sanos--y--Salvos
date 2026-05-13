package com.sanos.iamservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "RegisterRequest", description = "Cuerpo POST /api/iam/register. Persistencia en usuarios, contactos_usuario, credenciales.")
public record RegisterRequest(
        @Schema(description = "Nombre completo legal", example = "Ana Perez Lopez", requiredMode = Schema.RequiredMode.REQUIRED) String fullName,
        @Schema(description = "RUT formato 12345678-9", example = "12345678-9", requiredMode = Schema.RequiredMode.REQUIRED) String rutDocument,
        @Schema(description = "Correo unico", example = "ana@mail.cl", requiredMode = Schema.RequiredMode.REQUIRED) String email,
        @Schema(description = "Password en claro (se guarda hash BCrypt)", requiredMode = Schema.RequiredMode.REQUIRED) String password,
        @Schema(description = "Alias opcional; si vacio se deriva del nombre") String displayName,
        @Schema(description = "Comuna", example = "Providencia", requiredMode = Schema.RequiredMode.REQUIRED) String commune,
        @Schema(description = "Telefono Chile +56 9 ...", example = "+56 9 1234 5678", requiredMode = Schema.RequiredMode.REQUIRED) String phone,
        @Schema(description = "Direccion referencial", requiredMode = Schema.RequiredMode.REQUIRED) String address,
        @Schema(description = "Nombre contacto emergencia", requiredMode = Schema.RequiredMode.REQUIRED) String emergencyContactName,
        @Schema(description = "Telefono contacto emergencia", requiredMode = Schema.RequiredMode.REQUIRED) String emergencyContactPhone,
        @Schema(description = "Aceptacion terminos", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) Boolean acceptedTerms,
        @Schema(description = "Aceptacion politica privacidad", example = "true", requiredMode = Schema.RequiredMode.REQUIRED) Boolean acceptedPrivacyPolicy,
        @Schema(description = "Rol solicitado; produccion suele forzar CITIZEN", example = "CITIZEN") String role
) {}
