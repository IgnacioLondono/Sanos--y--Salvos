package com.sanos.petcatalogservice.repository;

import com.sanos.petcatalogservice.model.CaracteristicaFisica;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CaracteristicaFisicaRepository extends JpaRepository<CaracteristicaFisica, Long> {
    Optional<CaracteristicaFisica> findByIdMascota(Long idMascota);
}
