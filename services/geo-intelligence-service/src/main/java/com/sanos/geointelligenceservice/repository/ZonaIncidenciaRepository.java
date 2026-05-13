package com.sanos.geointelligenceservice.repository;

import com.sanos.geointelligenceservice.model.ZonaIncidencia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ZonaIncidenciaRepository extends JpaRepository<ZonaIncidencia, Long> {
    List<ZonaIncidencia> findByNombreComunaIgnoreCase(String nombreComuna);
}
