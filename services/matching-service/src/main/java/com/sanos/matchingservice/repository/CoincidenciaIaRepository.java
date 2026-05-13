package com.sanos.matchingservice.repository;

import com.sanos.matchingservice.model.CoincidenciaIa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CoincidenciaIaRepository extends JpaRepository<CoincidenciaIa, Long> {
    List<CoincidenciaIa> findByIdReportePerdida(Long idReportePerdida);
    List<CoincidenciaIa> findByIdReporteEncontrada(Long idReporteEncontrada);
}
