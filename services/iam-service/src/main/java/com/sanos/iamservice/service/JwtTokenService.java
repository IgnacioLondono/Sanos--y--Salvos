package com.sanos.iamservice.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Map;

@Service
public class JwtTokenService {

    private final SecretKey key;
    private final long ttlHours;

    public JwtTokenService(
            @Value("${sanos.jwt.secret}") String secret,
            @Value("${sanos.jwt.ttl-hours:4}") long ttlHours) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.ttlHours = ttlHours;
    }

    public String generateToken(String userId, String email, String role) {
        Instant now = Instant.now();
        Instant expiration = now.plus(ttlHours, ChronoUnit.HOURS);

        return Jwts.builder()
                .subject(userId)
                .claims(Map.of("email", email == null ? "" : email, "role", role == null ? "CITIZEN" : role))
                .issuedAt(Date.from(now))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }

    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
