package com.sanos.mediaservice.dto;

import java.util.List;

public record MediaDto(
        Long id,
        Long petId,
        Long reportId,
        String url,
        List<String> tags,
        String takenAt
) {}
