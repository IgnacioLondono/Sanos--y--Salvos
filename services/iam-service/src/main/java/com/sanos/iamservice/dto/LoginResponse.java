package com.sanos.iamservice.dto;

public record LoginResponse(
        String token,
        Long id,
        String email,
        String displayName,
        String role
) {}
