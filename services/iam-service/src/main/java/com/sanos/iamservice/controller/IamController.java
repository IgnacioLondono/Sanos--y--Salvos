package com.sanos.iamservice.controller;

import com.sanos.iamservice.dto.*;
import com.sanos.iamservice.model.Usuario;
import com.sanos.iamservice.repository.ContactoUsuarioRepository;
import com.sanos.iamservice.repository.UsuarioRepository;
import com.sanos.iamservice.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/iam")
@CrossOrigin(origins = "*")
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {
        try {
            LoginResponse resp = authService.login(req);
            return ResponseEntity.ok(resp);
        } catch (IllegalStateException | IllegalArgumentException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", ex.getMessage()));
        }
    }

    @GetMapping("/users")
    public List<UserDto> listUsers() {
        List<Usuario> usuarios = usuarioRepo.findAll();
        return usuarios.stream()
                .map(u -> UserDto.fromEntities(u,
                        contactoRepo.findByIdUsuario(u.getIdUsuario()).orElse(null)))
                .toList();
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<UserDto> userById(@PathVariable Long id) {
        return usuarioRepo.findById(id)
                .map(u -> UserDto.fromEntities(u, contactoRepo.findByIdUsuario(id).orElse(null)))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/health")
    public Map<String, String> health() {
        return Map.of("status", "UP", "service", "iam-service");
    }
}
