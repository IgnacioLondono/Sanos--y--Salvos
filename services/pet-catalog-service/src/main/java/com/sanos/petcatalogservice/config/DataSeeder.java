package com.sanos.petcatalogservice.config;

import com.sanos.petcatalogservice.dto.PetDto;
import com.sanos.petcatalogservice.repository.MascotaRepository;
import com.sanos.petcatalogservice.service.PetService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataSeeder implements CommandLineRunner {

    private final MascotaRepository repo;
    private final PetService service;

    public DataSeeder(MascotaRepository repo, PetService service) {
        this.repo = repo;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        service.create(new PetDto(null, "Milo", "DOG", "Labrador", "Dorado", "Mediano",
                "CHIP-0001", 2L, null));
        service.create(new PetDto(null, "Luna", "CAT", "Siames", "Blanco", "Pequeno",
                "CHIP-0002", 2L, null));
        service.create(new PetDto(null, "Rocky", "DOG", "Mestizo", "Negro", "Grande",
                "CHIP-0003", null, null));
    }
}
