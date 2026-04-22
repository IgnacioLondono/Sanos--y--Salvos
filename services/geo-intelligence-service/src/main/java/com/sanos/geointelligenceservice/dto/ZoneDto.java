package com.sanos.geointelligenceservice.dto;

import java.math.BigDecimal;

public record ZoneDto(
        Long id,
        String commune,
        String riskLevel,
        BigDecimal latitude,
        BigDecimal longitude,
        Long reportId
) {}
