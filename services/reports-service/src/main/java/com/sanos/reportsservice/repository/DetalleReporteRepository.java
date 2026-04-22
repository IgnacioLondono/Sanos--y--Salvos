package com.sanos.reportsservice.repository;

import com.sanos.reportsservice.model.DetalleReporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DetalleReporteRepository extends JpaRepository<DetalleReporte, Long> {
    Optional<DetalleReporte> findByIdReporte(Long idReporte);
}
