package com.sanos.capacityservice.dto;

public record CapacityDto(
        Long id,
        String name,
        String organization,
        String zone,
        Integer volunteers,
        Integer hoursAvailable,
        String availableFrom,
        String createdAt
) {}
