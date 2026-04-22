package com.sanos.capacityservice.repository;

import com.sanos.capacityservice.model.AsignacionCapacidad;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AsignacionCapacidadRepository extends JpaRepository<AsignacionCapacidad, Long> {
    List<AsignacionCapacidad> findByIdEquipo(Long idEquipo);
    List<AsignacionCapacidad> findByIdUsuario(Long idUsuario);
}
