package com.sanos.capacityservice.repository;

import com.sanos.capacityservice.model.EquipoColaboracion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipoColaboracionRepository extends JpaRepository<EquipoColaboracion, Long> {
    List<EquipoColaboracion> findByZonaOperacionIgnoreCase(String zonaOperacion);
}
