package com.sanos.petcatalogservice.repository;

import com.sanos.petcatalogservice.model.Mascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MascotaRepository extends JpaRepository<Mascota, Long> {
    Optional<Mascota> findByNumeroChip(String numeroChip);
}
