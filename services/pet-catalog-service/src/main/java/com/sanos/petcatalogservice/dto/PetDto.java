package com.sanos.petcatalogservice.dto;

public record PetDto(
        Long id,
        String name,
        String species,
        String breed,
        String color,
        String size,
        String chipNumber,
        Long ownerId,
        String createdAt
) {}
