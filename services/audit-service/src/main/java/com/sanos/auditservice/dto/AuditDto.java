package com.sanos.auditservice.dto;

public record AuditDto(
        Long id,
        String entity,
        String operation,
        String actor,
        String changes,
        String createdAt
) {}
