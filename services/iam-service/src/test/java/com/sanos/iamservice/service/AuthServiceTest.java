package com.sanos.iamservice.service;

import com.sanos.iamservice.dto.*;
import com.sanos.iamservice.model.ContactoUsuario;
import com.sanos.iamservice.model.Credencial;
import com.sanos.iamservice.model.Usuario;
import com.sanos.iamservice.repository.ContactoUsuarioRepository;
import com.sanos.iamservice.repository.CredencialRepository;
import com.sanos.iamservice.repository.UsuarioRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepo;
    @Mock
    private CredencialRepository credencialRepo;
    @Mock
    private ContactoUsuarioRepository contactoRepo;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtTokenService jwtTokenService;
    @Mock
    private Claims claims;

    private AuthService service;

    @BeforeEach
    void setUp() {
        service = new AuthService(usuarioRepo, credencialRepo, contactoRepo, passwordEncoder, jwtTokenService);
    }

    @Test
    void register_createsUserContactoAndCredential() {
        RegisterRequest req = new RegisterRequest(
                "Ana Perez", "12.345.678-9", "Ana@Mail.cl", "1234", null,
                "Santiago", "+56912345678", "Calle 123",
                "Luis", "+56988888888", true, true, null
        );
        when(contactoRepo.findByCorreoElectronico("ana@mail.cl")).thenReturn(Optional.empty());
        when(usuarioRepo.findByRutDocumento("12345678-9")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("1234")).thenReturn("HASHED");
        when(usuarioRepo.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setIdUsuario(50L);
            return u;
        });
        when(contactoRepo.save(any(ContactoUsuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UserDto dto = service.register(req);

        assertEquals(50L, dto.id());
        assertEquals("ana@mail.cl", dto.email());
        assertEquals("CITIZEN", dto.role());
        verify(credencialRepo).save(any(Credencial.class));
    }

    @Test
    void register_throwsWhenEmailAlreadyExists() {
        RegisterRequest req = new RegisterRequest(
                "Ana", "1-9", "ana@mail.cl", "1234", null,
                "SCL", "9", "a", "b", "c", true, true, "CITIZEN"
        );
        when(contactoRepo.findByCorreoElectronico("ana@mail.cl")).thenReturn(Optional.of(new ContactoUsuario()));

        assertThrows(IllegalStateException.class, () -> service.register(req));
    }

    @Test
    void login_returnsTokenAndProfile() {
        LoginRequest req = new LoginRequest("user@mail.cl", "1234");
        ContactoUsuario contacto = new ContactoUsuario();
        contacto.setIdUsuario(3L);
        contacto.setCorreoElectronico("user@mail.cl");

        Credencial credencial = new Credencial();
        credencial.setIdUsuario(3L);
        credencial.setPasswordHash("HASH");

        Usuario usuario = usuario(3L, "ADMIN");

        when(contactoRepo.findByCorreoElectronico("user@mail.cl")).thenReturn(Optional.of(contacto));
        when(credencialRepo.findByIdUsuario(3L)).thenReturn(Optional.of(credencial));
        when(passwordEncoder.matches("1234", "HASH")).thenReturn(true);
        when(usuarioRepo.findById(3L)).thenReturn(Optional.of(usuario));
        when(jwtTokenService.generateToken("3", "user@mail.cl", "ADMIN")).thenReturn("token-123");

        LoginResponse response = service.login(req);

        assertEquals("token-123", response.token());
        assertEquals(3L, response.id());
        assertEquals("ADMIN", response.role());
    }

    @Test
    void login_throwsWhenPasswordDoesNotMatch() {
        ContactoUsuario contacto = new ContactoUsuario();
        contacto.setIdUsuario(4L);
        Credencial credencial = new Credencial();
        credencial.setPasswordHash("HASH");

        when(contactoRepo.findByCorreoElectronico("x@mail.cl")).thenReturn(Optional.of(contacto));
        when(credencialRepo.findByIdUsuario(4L)).thenReturn(Optional.of(credencial));
        when(passwordEncoder.matches("bad", "HASH")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> service.login(new LoginRequest("x@mail.cl", "bad")));
    }

    @Test
    void profileFromToken_throwsWhenTokenInvalid() {
        when(jwtTokenService.parseClaims("Bearer x")).thenThrow(new JwtException("bad"));

        assertThrows(IllegalStateException.class, () -> service.profileFromToken("Bearer x"));
    }

    @Test
    void updateUserRole_requiresAdminRole() {
        when(jwtTokenService.parseClaims("Bearer t")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn("CITIZEN");

        assertThrows(IllegalStateException.class, () -> service.updateUserRole("Bearer t", 2L, "ADMIN"));
    }

    @Test
    void updateUserRole_rejectsRemovingLastAdmin() {
        Usuario target = usuario(2L, "ADMIN");

        when(jwtTokenService.parseClaims("Bearer t")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(usuarioRepo.findById(2L)).thenReturn(Optional.of(target));
        when(usuarioRepo.findAll()).thenReturn(List.of(target));

        assertThrows(IllegalStateException.class, () -> service.updateUserRole("Bearer t", 2L, "CITIZEN"));
    }

    @Test
    void deleteUser_throwsWhenTryingToDeleteSelf() {
        when(jwtTokenService.parseClaims("Bearer admin")).thenReturn(claims);
        when(claims.get("role", String.class)).thenReturn("ADMIN");
        when(claims.getSubject()).thenReturn("5");

        assertThrows(IllegalStateException.class, () -> service.deleteUser("Bearer admin", 5L));
    }

    @Test
    void changePassword_updatesCredentialHash() {
        Credencial cred = new Credencial();
        cred.setIdUsuario(10L);
        cred.setPasswordHash("OLD");

        when(jwtTokenService.parseClaims("Bearer t")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("10");
        when(credencialRepo.findByIdUsuario(10L)).thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("actual", "OLD")).thenReturn(true);
        when(passwordEncoder.encode("nueva123")).thenReturn("NEW");

        service.changePassword("Bearer t", "actual", "nueva123");

        assertEquals("NEW", cred.getPasswordHash());
        verify(credencialRepo).save(cred);
    }

    @Test
    void changePassword_throwsWhenCurrentPasswordInvalid() {
        Credencial cred = new Credencial();
        cred.setPasswordHash("OLD");

        when(jwtTokenService.parseClaims("Bearer t")).thenReturn(claims);
        when(claims.getSubject()).thenReturn("10");
        when(credencialRepo.findByIdUsuario(10L)).thenReturn(Optional.of(cred));
        when(passwordEncoder.matches("bad", "OLD")).thenReturn(false);

        assertThrows(IllegalStateException.class, () -> service.changePassword("Bearer t", "bad", "nueva123"));
    }

    private Usuario usuario(Long id, String role) {
        Usuario u = new Usuario();
        u.setIdUsuario(id);
        u.setNombreCompleto("Nombre Usuario");
        u.setRol(role);
        u.setFechaRegistro(LocalDateTime.now());
        return u;
    }
}
