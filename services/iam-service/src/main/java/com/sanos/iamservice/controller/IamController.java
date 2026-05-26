package com.sanos.iamservice.controller;

import com.sanos.iamservice.dto.*;
import com.sanos.iamservice.model.Usuario;
import com.sanos.iamservice.repository.ContactoUsuarioRepository;
import com.sanos.iamservice.repository.UsuarioRepository;
import com.sanos.iamservice.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iam")
@CrossOrigin(origins = "*")
@Tag(name = "IAM", description = "Identidad, registro, login y listado de usuarios. Tablas: usuarios, credenciales, contactos_usuario (db_iam).")
public class IamController {

    private final AuthService authService;
    private final UsuarioRepository usuarioRepo;
    private final ContactoUsuarioRepository contactoRepo;

    public IamController(AuthService authService,
                         UsuarioRepository usuarioRepo,
                         ContactoUsuarioRepository contactoRepo) {
        this.authService = authService;
        this.usuarioRepo = usuarioRepo;
        this.contactoRepo = contactoRepo;
    }

    @Operation(summary = "Registrar usuario", description = "Crea filas en usuarios, contactos_usuario y credenciales (password con BCrypt).")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Usuario creado",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Validacion fallida",
                    content = @Content(schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "409", description = "Correo o RUT ya existente",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        try {
            UserDto dto = authService.register(req);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Iniciar sesion", description = "Valida credenciales y devuelve JWT firmado (HMAC).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login correcto",
                    content = @Content(schema = @Schema(implementation = LoginResponse.class))),
            @ApiResponse(responseCode = "401", description = "Credenciales invalidas",
                    content = @Content(schema = @Schema(implementation = Map.class)))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            LoginResponse resp = authService.login(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Perfil del usuario autenticado")
    @GetMapping({"/profile", "/users/me"})
    public ResponseEntity<?> currentUser(
            @RequestHeader(value = "Authorization", required = false) String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token requerido"));
        }
        try {
            return ResponseEntity.ok(authService.profileFromToken(authorization.substring(7)));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Actualizar perfil del usuario autenticado")
    @PatchMapping({"/profile", "/users/me"})
    public ResponseEntity<?> updateProfile(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody UpdateProfileRequest body) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token requerido"));
        }
        try {
            UserDto dto = authService.updateProfile(authorization.substring(7), body);
            return ResponseEntity.ok(dto);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Cambiar contrasena", description = "Requiere Authorization: Bearer JWT del usuario. Actualiza hash en credenciales.")
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @RequestBody ChangePasswordRequest req) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token requerido"));
        }
        String token = authorization.substring(7);
        try {
            authService.changePassword(token, req.currentPassword(), req.newPassword());
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Crear administrador (solo ADMIN)", description = "Registra un usuario nuevo con rol ADMIN.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Administrador creado",
                    content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "400", description = "Datos invalidos",
                    content = @Content(schema = @Schema(implementation = ApiErrorDto.class))),
            @ApiResponse(responseCode = "401", description = "Token ausente o invalido",
                    content = @Content(schema = @Schema(implementation = ApiErrorDto.class))),
            @ApiResponse(responseCode = "409", description = "Correo o RUT duplicado",
                    content = @Content(schema = @Schema(implementation = ApiErrorDto.class)))
    })
    @PostMapping("/admin/users")
    public ResponseEntity<?> createAdmin(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    required = true,
                    content = @Content(schema = @Schema(implementation = AdminCreateRequest.class)))
            @RequestBody AdminCreateRequest req) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token requerido"));
        }
        try {
            UserDto dto = authService.registerAdmin(authorization.substring(7), req);
            return ResponseEntity.status(HttpStatus.CREATED).body(dto);
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", ex.getMessage()));
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Listar usuarios", description = "Devuelve todos los usuarios con contacto; requiere JWT en gateway.")
    @ApiResponse(responseCode = "200", description = "Lista de UserDto",
            content = @Content(schema = @Schema(implementation = UserDto.class)))
    @GetMapping("/users")
    public List<UserDto> listUsers() {
        List<Usuario> usuarios = usuarioRepo.findAll();
        return usuarios.stream()
                .map(u -> UserDto.fromEntities(u,
                        contactoRepo.findByIdUsuario(u.getIdUsuario()).orElse(null)))
                .toList();
    }

    @Operation(summary = "Usuario por ID", description = "Busca por id_usuario y arma UserDto con contacto.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", content = @Content(schema = @Schema(implementation = UserDto.class))),
            @ApiResponse(responseCode = "404", description = "No encontrado")
    })
    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> userById(
            @Parameter(description = "PK usuarios.id_usuario", example = "1", required = true) @PathVariable Long id) {
        return usuarioRepo.findById(id)
                .map(u -> UserDto.fromEntities(u, contactoRepo.findByIdUsuario(id).orElse(null)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(summary = "Actualizar rol (solo ADMIN)", description = "JWT con claim role=ADMIN. No permite dejar sin administradores.")
    @PatchMapping("/users/{id}/role")
    public ResponseEntity<?> patchUserRole(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "id_usuario", required = true) @PathVariable Long id,
            @RequestBody UpdateRoleRequest body) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token requerido"));
        }
        try {
            UserDto dto = authService.updateUserRole(authorization.substring(7), id, body.role());
            return ResponseEntity.ok(dto);
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().body(Map.of("error", ex.getMessage()));
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Eliminar usuario (solo ADMIN)", description = "Borra credencial, contacto y usuario. No auto-eliminacion ni unico admin.")
    @DeleteMapping("/users/{id}")
    public ResponseEntity<?> deleteUser(
            @RequestHeader(value = "Authorization", required = false) String authorization,
            @Parameter(description = "id_usuario", required = true) @PathVariable Long id) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Token requerido"));
        }
        try {
            authService.deleteUser(authorization.substring(7), id);
            return ResponseEntity.noContent().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Salud del servicio", description = "Endpoint publico para healthchecks.")
    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "iam-service");
    }
}
