package com.sanos.iamservice.dto;

import com.sanos.iamservice.model.ContactoUsuario;
import com.sanos.iamservice.model.Usuario;
import io.swagger.v3.oas.annotations.media.Schema;

@Schema(name = "UserDto", description = "Vista de usuario para API (mapea tablas usuarios + contactos_usuario).")
public record UserDto(
        @Schema(description = "PK usuarios.id_usuario", example = "1") Long id,
        @Schema(description = "Correo (contactos_usuario.correo_electronico)", example = "user@mail.cl") String email,
        @Schema(description = "Nombre corto para UI", example = "Ana Perez") String displayName,
        @Schema(description = "Nombre completo registrado") String fullName,
        @Schema(description = "RUT con digito verificador", example = "12345678-9") String rut,
        @Schema(description = "Comuna de residencia", example = "Providencia") String commune,
        @Schema(description = "Direccion referencial") String address,
        @Schema(description = "Telefono principal", example = "+56 9 1234 5678") String phone,
        @Schema(description = "Nombre contacto de emergencia") String emergencyContactName,
        @Schema(description = "Telefono contacto de emergencia") String emergencyContactPhone,
        @Schema(description = "Rol IAM", example = "CITIZEN", allowableValues = {"CITIZEN", "ADMIN"}) String role,
        @Schema(description = "Fecha registro ISO-8601", example = "2026-04-23T10:00:00") String createdAt
) {
    public static UserDto fromEntities(Usuario usuario, ContactoUsuario contacto) {
        String email = contacto != null ? contacto.getCorreoElectronico() : null;
        String phone = contacto != null ? contacto.getTelefonoPrincipal() : null;
        String created = usuario.getFechaRegistro() != null ? usuario.getFechaRegistro().toString() : null;
        String displayName = deriveDisplayName(usuario.getNombreCompleto());
        return new UserDto(
                usuario.getIdUsuario(),
                email,
                displayName,
                usuario.getNombreCompleto(),
                usuario.getRutDocumento(),
                usuario.getComuna(),
                usuario.getDireccion(),
                phone,
                usuario.getContactoEmergenciaNombre(),
                usuario.getContactoEmergenciaTelefono(),
                usuario.getRol(),
                created);
    }

    private static String deriveDisplayName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "Usuario";
        String[] parts = fullName.trim().split("\\s+");
        if (parts.length == 1) return parts[0];
        return parts[0] + " " + parts[1];
    }
}
