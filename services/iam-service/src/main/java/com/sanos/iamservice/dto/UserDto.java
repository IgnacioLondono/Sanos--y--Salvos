package com.sanos.iamservice.dto;

import com.sanos.iamservice.model.ContactoUsuario;
import com.sanos.iamservice.model.Usuario;

public record UserDto(
        Long id,
        String email,
        String displayName,
        String rut,
        String commune,
        String address,
        String phone,
        String emergencyContactName,
        String emergencyContactPhone,
        String role,
        String createdAt
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
