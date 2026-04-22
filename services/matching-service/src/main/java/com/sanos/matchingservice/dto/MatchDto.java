package com.sanos.matchingservice.dto;

public record MatchDto(
        Long id,
        Long lostReportId,
        Long foundReportId,
        Float score,
        String explanation,
        String createdAt
) {}
