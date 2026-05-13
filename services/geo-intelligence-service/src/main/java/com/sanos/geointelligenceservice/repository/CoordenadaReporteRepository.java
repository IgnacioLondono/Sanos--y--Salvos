package com.sanos.geointelligenceservice.repository;

import com.sanos.geointelligenceservice.model.CoordenadaReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoordenadaReporteRepository extends JpaRepository<CoordenadaReporte, Long> {
    List<CoordenadaReporte> findByIdReporte(Long idReporte);
}
