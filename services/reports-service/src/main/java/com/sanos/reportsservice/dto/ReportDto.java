package com.sanos.reportsservice.dto;

import java.math.BigDecimal;

public record ReportDto(
        Long id,
        Long petId,
        Long createdBy,
        String type,
        String status,
        String commune,
        String description,
        String healthStatus,
        BigDecimal latitude,
        BigDecimal longitude,
        String createdAt
) {}
