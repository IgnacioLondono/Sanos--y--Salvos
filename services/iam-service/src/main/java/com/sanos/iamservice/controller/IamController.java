package com.sanos.iamservice.controller;

import com.sanos.iamservice.service.JwtTokenService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/api/iam")
public class IamController {

    private final Map<String, Map<String, Object>> users = new ConcurrentHashMap<>();
    private final JwtTokenService jwtTokenService;

    public IamController(JwtTokenService jwtTokenService) {
        this.jwtTokenService = jwtTokenService;
    }

    @GetMapping("/health")
    public Map<String, Object> health() {
        return Map.of(
                "service", "iam-service",
                "status", "UP",
                "timestamp", Instant.now().toString(),
                "users", users.size()
        );
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@RequestBody Map<String, Object> body) {
        String email = String.valueOf(body.getOrDefault("email", "")).trim().toLowerCase(Locale.ROOT);
        String password = String.valueOf(body.getOrDefault("password", "")).trim();

        if (email.isBlank() || password.isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "email and password are required"));
        }

        boolean emailInUse = users.values().stream().anyMatch(u -> email.equals(u.get("email")));
        if (emailInUse) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "email already registered"));
        }

        String id = UUID.randomUUID().toString();
        Map<String, Object> user = new LinkedHashMap<>();
        user.put("id", id);
        user.put("email", email);
        user.put("passwordHash", BCrypt.hashpw(password, BCrypt.gensalt()));
        user.put("displayName", body.getOrDefault("displayName", email.split("@")[0]));
        user.put("phone", body.getOrDefault("phone", ""));
        user.put("role", body.getOrDefault("role", "CITIZEN"));
        user.put("createdAt", Instant.now().toString());

        users.put(id, user);

        Map<String, Object> response = new LinkedHashMap<>(user);
        response.remove("passwordHash");
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@RequestBody Map<String, Object> body) {
        String email = String.valueOf(body.getOrDefault("email", "")).trim().toLowerCase(Locale.ROOT);
        String password = String.valueOf(body.getOrDefault("password", "")).trim();

        Optional<Map<String, Object>> found = users.values().stream()
                .filter(u -> email.equals(u.get("email")))
                .findFirst();

        if (found.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
        }

        Map<String, Object> user = found.get();
        if (!BCrypt.checkpw(password, String.valueOf(user.get("passwordHash")))) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "invalid credentials"));
        }

        String token = jwtTokenService.generateToken(
                String.valueOf(user.get("id")),
                String.valueOf(user.get("email")),
                String.valueOf(user.get("role"))
        );

        return ResponseEntity.ok(Map.of(
                "token", token,
                "id", user.get("id"),
                "email", user.get("email"),
                "displayName", user.get("displayName"),
                "role", user.get("role")
        ));
    }

    @GetMapping("/users")
    public List<Map<String, Object>> users() {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Map<String, Object> user : users.values()) {
            Map<String, Object> clean = new LinkedHashMap<>(user);
            clean.remove("passwordHash");
            result.add(clean);
        }
        return result;
    }

    @GetMapping("/users/{id}")
    public ResponseEntity<Map<String, Object>> userById(@PathVariable String id) {
        Map<String, Object> user = users.get(id);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> clean = new LinkedHashMap<>(user);
        clean.remove("passwordHash");
        return ResponseEntity.ok(clean);
    }
}
