package com.sanos.iamservice.dto;

public record RegisterRequest(
        String fullName,
        String rutDocument,
        String email,
        String password,
        String displayName,
        String commune,
        String phone,
        String address,
        String emergencyContactName,
        String emergencyContactPhone,
        Boolean acceptedTerms,
        Boolean acceptedPrivacyPolicy,
        String role
) {}
