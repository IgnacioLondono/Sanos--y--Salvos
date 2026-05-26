package com.sanos.iamservice.service;

import com.sanos.iamservice.dto.*;
import com.sanos.iamservice.model.ContactoUsuario;
import com.sanos.iamservice.model.Credencial;
import com.sanos.iamservice.model.Usuario;
import com.sanos.iamservice.repository.ContactoUsuarioRepository;
import com.sanos.iamservice.repository.CredencialRepository;
import com.sanos.iamservice.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;

import java.time.LocalDateTime;

@Service
public class AuthService {

    private final UsuarioRepository usuarioRepo;
    private final CredencialRepository credencialRepo;
    private final ContactoUsuarioRepository contactoRepo;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService jwtTokenService;

    public AuthService(UsuarioRepository usuarioRepo,
                       CredencialRepository credencialRepo,
                       ContactoUsuarioRepository contactoRepo,
                       PasswordEncoder passwordEncoder,
                       JwtTokenService jwtTokenService) {
        this.usuarioRepo = usuarioRepo;
        this.credencialRepo = credencialRepo;
        this.contactoRepo = contactoRepo;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenService = jwtTokenService;
    }

    @Transactional
    public UserDto register(RegisterRequest req) {
        if (req == null || req.email() == null || req.password() == null) {
            throw new IllegalArgumentException("email y password son obligatorios");
        }

        String email = req.email().trim().toLowerCase();
        if (contactoRepo.findByCorreoElectronico(email).isPresent()) {
            throw new IllegalStateException("El correo ya esta registrado");
        }

        String rut = normalizeRut(req.rutDocument());
        if (rut != null && usuarioRepo.findByRutDocumento(rut).isPresent()) {
            throw new IllegalStateException("El RUT ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setRutDocumento(rut);
        usuario.setNombreCompleto(req.fullName());
        usuario.setComuna(req.commune());
        usuario.setDireccion(req.address());
        usuario.setContactoEmergenciaNombre(req.emergencyContactName());
        usuario.setContactoEmergenciaTelefono(req.emergencyContactPhone());
        usuario.setAceptoTerminos(Boolean.TRUE.equals(req.acceptedTerms()));
        usuario.setAceptoPrivacidad(Boolean.TRUE.equals(req.acceptedPrivacyPolicy()));
        usuario.setRol("CITIZEN");
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario = usuarioRepo.save(usuario);

        ContactoUsuario contacto = new ContactoUsuario();
        contacto.setIdUsuario(usuario.getIdUsuario());
        contacto.setCorreoElectronico(email);
        contacto.setTelefonoPrincipal(req.phone());
        contacto = contactoRepo.save(contacto);

        Credencial credencial = new Credencial();
        credencial.setIdUsuario(usuario.getIdUsuario());
        credencial.setPasswordHash(passwordEncoder.encode(req.password()));
        credencial.setEstadoCuenta("ACTIVA");
        credencialRepo.save(credencial);

        return UserDto.fromEntities(usuario, contacto);
    }

    @Transactional
    public UserDto registerAdmin(String bearerToken, AdminCreateRequest req) {
        requireAdminToken(bearerToken);
        if (req == null || req.email() == null || req.password() == null || req.fullName() == null) {
            throw new IllegalArgumentException("nombre, correo y contrasena son obligatorios");
        }
        if (req.rutDocument() == null || req.rutDocument().isBlank()) {
            throw new IllegalArgumentException("RUT obligatorio");
        }

        RegisterRequest registerRequest = new RegisterRequest(
                req.fullName().trim(),
                req.rutDocument().trim(),
                req.email().trim(),
                req.password(),
                req.fullName().trim(),
                req.commune() == null || req.commune().isBlank() ? "Santiago" : req.commune().trim(),
                req.phone() == null || req.phone().isBlank() ? "+56 9 0000 0000" : req.phone().trim(),
                "Administracion Sanos y Salvos",
                "Soporte",
                "+56 2 2000 0000",
                true,
                true,
                "ADMIN"
        );

        String email = registerRequest.email().trim().toLowerCase();
        if (contactoRepo.findByCorreoElectronico(email).isPresent()) {
            throw new IllegalStateException("El correo ya esta registrado");
        }

        String rut = normalizeRut(registerRequest.rutDocument());
        if (rut != null && usuarioRepo.findByRutDocumento(rut).isPresent()) {
            throw new IllegalStateException("El RUT ya esta registrado");
        }

        Usuario usuario = new Usuario();
        usuario.setRutDocumento(rut);
        usuario.setNombreCompleto(registerRequest.fullName());
        usuario.setComuna(registerRequest.commune());
        usuario.setDireccion(registerRequest.address());
        usuario.setContactoEmergenciaNombre(registerRequest.emergencyContactName());
        usuario.setContactoEmergenciaTelefono(registerRequest.emergencyContactPhone());
        usuario.setAceptoTerminos(true);
        usuario.setAceptoPrivacidad(true);
        usuario.setRol("ADMIN");
        usuario.setFechaRegistro(LocalDateTime.now());
        usuario = usuarioRepo.save(usuario);

        ContactoUsuario contacto = new ContactoUsuario();
        contacto.setIdUsuario(usuario.getIdUsuario());
        contacto.setCorreoElectronico(email);
        contacto.setTelefonoPrincipal(registerRequest.phone());
        contacto = contactoRepo.save(contacto);

        Credencial credencial = new Credencial();
        credencial.setIdUsuario(usuario.getIdUsuario());
        credencial.setPasswordHash(passwordEncoder.encode(registerRequest.password()));
        credencial.setEstadoCuenta("ACTIVA");
        credencialRepo.save(credencial);

        return UserDto.fromEntities(usuario, contacto);
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        if (req == null || req.email() == null || req.password() == null) {
            throw new IllegalArgumentException("email y password son obligatorios");
        }

        String email = req.email().trim().toLowerCase();
        ContactoUsuario contacto = contactoRepo.findByCorreoElectronico(email)
                .orElseThrow(() -> new IllegalStateException("Credenciales invalidas"));

        Credencial credencial = credencialRepo.findByIdUsuario(contacto.getIdUsuario())
                .orElseThrow(() -> new IllegalStateException("Credenciales invalidas"));

        if (!passwordEncoder.matches(req.password(), credencial.getPasswordHash())) {
            throw new IllegalStateException("Credenciales invalidas");
        }

        Usuario usuario = usuarioRepo.findById(contacto.getIdUsuario())
                .orElseThrow(() -> new IllegalStateException("Usuario inexistente"));

        String role = usuario.getRol() == null ? "CITIZEN" : usuario.getRol();
        UserDto dto = UserDto.fromEntities(usuario, contacto);
        String token = jwtTokenService.generateToken(String.valueOf(usuario.getIdUsuario()), email, role);

        return new LoginResponse(token, dto.id(), dto.email(), dto.displayName(), role);
    }

    @Transactional(readOnly = true)
    public UserDto profileFromToken(String bearerToken) {
        long userId = userIdFromToken(bearerToken);
        Usuario usuario = usuarioRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        var contacto = contactoRepo.findByIdUsuario(userId).orElse(null);
        return UserDto.fromEntities(usuario, contacto);
    }

    @Transactional
    public UserDto updateProfile(String bearerToken, UpdateProfileRequest req) {
        long userId = userIdFromToken(bearerToken);
        Usuario usuario = usuarioRepo.findById(userId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));

        if (req.fullName() != null && !req.fullName().isBlank()) {
            usuario.setNombreCompleto(req.fullName().trim());
        }
        if (req.commune() != null) {
            usuario.setComuna(req.commune().trim());
        }
        if (req.address() != null) {
            usuario.setDireccion(req.address().trim());
        }
        if (req.emergencyContactName() != null) {
            usuario.setContactoEmergenciaNombre(req.emergencyContactName().trim());
        }
        if (req.emergencyContactPhone() != null) {
            usuario.setContactoEmergenciaTelefono(req.emergencyContactPhone().trim());
        }
        usuarioRepo.save(usuario);

        ContactoUsuario contacto = contactoRepo.findByIdUsuario(userId).orElse(null);
        if (contacto != null && req.phone() != null) {
            contacto.setTelefonoPrincipal(req.phone().trim());
            contactoRepo.save(contacto);
        }

        return UserDto.fromEntities(usuario, contacto);
    }

    private long userIdFromToken(String bearerToken) {
        try {
            return Long.parseLong(jwtTokenService.parseClaims(bearerToken).getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalStateException("Token invalido");
        }
    }

    @Transactional
    public void changePassword(String bearerToken, String currentPassword, String newPassword) {
        if (currentPassword == null || newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Contrasena actual y nueva son obligatorias");
        }
        if (newPassword.length() < 4) {
            throw new IllegalArgumentException("La nueva contrasena debe tener al menos 4 caracteres");
        }
        long userId;
        try {
            userId = Long.parseLong(jwtTokenService.parseClaims(bearerToken).getSubject());
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalStateException("Token invalido");
        }

        Credencial credencial = credencialRepo.findByIdUsuario(userId)
                .orElseThrow(() -> new IllegalStateException("Credenciales no encontradas"));

        if (!passwordEncoder.matches(currentPassword, credencial.getPasswordHash())) {
            throw new IllegalStateException("Contrasena actual incorrecta");
        }

        credencial.setPasswordHash(passwordEncoder.encode(newPassword));
        credencialRepo.save(credencial);
    }

    private Claims requireAdminToken(String bearerToken) {
        Claims claims;
        try {
            claims = jwtTokenService.parseClaims(bearerToken);
        } catch (JwtException | IllegalArgumentException ex) {
            throw new IllegalStateException("Token invalido");
        }
        String role = claims.get("role", String.class);
        if (role == null || !"ADMIN".equalsIgnoreCase(role)) {
            throw new IllegalStateException("Requiere rol administrador");
        }
        return claims;
    }

    private void ensureNotRemovingLastAdmin(Usuario target, String requestedRole) {
        if (target.getRol() == null || !"ADMIN".equalsIgnoreCase(target.getRol())) {
            return;
        }
        if (requestedRole != null && "ADMIN".equalsIgnoreCase(requestedRole)) {
            return;
        }
        long admins = usuarioRepo.findAll().stream()
                .filter(u -> u.getRol() != null && "ADMIN".equalsIgnoreCase(u.getRol()))
                .count();
        if (admins <= 1) {
            throw new IllegalStateException("No se puede quitar el ultimo administrador");
        }
    }

    private boolean isOnlyAdmin(Usuario u) {
        if (u.getRol() == null || !"ADMIN".equalsIgnoreCase(u.getRol())) {
            return false;
        }
        return usuarioRepo.findAll().stream()
                .filter(x -> x.getRol() != null && "ADMIN".equalsIgnoreCase(x.getRol()))
                .count() <= 1;
    }

    @Transactional
    public UserDto updateUserRole(String bearerToken, Long targetUserId, String newRole) {
        requireAdminToken(bearerToken);
        if (newRole == null || newRole.isBlank()) {
            throw new IllegalArgumentException("Rol obligatorio");
        }
        String normalized = newRole.trim().toUpperCase();
        if (!"ADMIN".equals(normalized) && !"CITIZEN".equals(normalized)) {
            throw new IllegalArgumentException("Rol debe ser ADMIN o CITIZEN");
        }
        Usuario u = usuarioRepo.findById(targetUserId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        ensureNotRemovingLastAdmin(u, normalized);
        u.setRol(normalized);
        usuarioRepo.save(u);
        var contacto = contactoRepo.findByIdUsuario(targetUserId).orElse(null);
        return UserDto.fromEntities(u, contacto);
    }

    @Transactional
    public void deleteUser(String bearerToken, Long targetUserId) {
        Claims claims = requireAdminToken(bearerToken);
        long actorId = Long.parseLong(claims.getSubject());
        if (actorId == targetUserId) {
            throw new IllegalStateException("No puedes eliminar tu propia cuenta");
        }
        Usuario u = usuarioRepo.findById(targetUserId)
                .orElseThrow(() -> new IllegalStateException("Usuario no encontrado"));
        if (isOnlyAdmin(u)) {
            throw new IllegalStateException("No se puede eliminar el unico administrador");
        }
        credencialRepo.findByIdUsuario(targetUserId).ifPresent(credencialRepo::delete);
        contactoRepo.findByIdUsuario(targetUserId).ifPresent(contactoRepo::delete);
        usuarioRepo.delete(u);
    }

    private String normalizeRut(String value) {
        if (value == null) return null;
        return value.trim().replace(".", "").toUpperCase();
    }
}
