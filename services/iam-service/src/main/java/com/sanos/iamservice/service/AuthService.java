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
        usuario.setRol(req.role() == null ? "CITIZEN" : req.role().toUpperCase());
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

    private String normalizeRut(String value) {
        if (value == null) return null;
        return value.trim().replace(".", "").toUpperCase();
    }
}
