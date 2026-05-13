package com.sanos.reportsservice.repository;

import com.sanos.reportsservice.model.ReporteEvento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReporteEventoRepository extends JpaRepository<ReporteEvento, Long> {
    List<ReporteEvento> findByIdMascota(Long idMascota);
    List<ReporteEvento> findByEstado(String estado);
    List<ReporteEvento> findByTipoReporte(String tipoReporte);
}
