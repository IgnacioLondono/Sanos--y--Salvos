package com.sanos.iamservice.service;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenServiceTest {

    private static final String SECRET = "sanos-y-salvos-super-secret-key-at-least-32-chars";

    @Test
    void generateAndParseToken_returnsExpectedClaims() {
        JwtTokenService jwt = new JwtTokenService(SECRET, 4);

        String token = jwt.generateToken("15", "test@mail.cl", "ADMIN");
        Claims claims = jwt.parseClaims(token);

        assertEquals("15", claims.getSubject());
        assertEquals("test@mail.cl", claims.get("email", String.class));
        assertEquals("ADMIN", claims.get("role", String.class));
        assertNotNull(claims.getExpiration());
        assertNotNull(claims.getIssuedAt());
    }

    @Test
    void generateToken_defaultsRoleToCitizenWhenNull() {
        JwtTokenService jwt = new JwtTokenService(SECRET, 1);

        String token = jwt.generateToken("20", "u@mail.cl", null);
        Claims claims = jwt.parseClaims(token);

        assertEquals("CITIZEN", claims.get("role", String.class));
    }
}
