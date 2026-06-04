package com.sanos.reportsservice.repository;

import com.sanos.reportsservice.model.SolicitudContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SolicitudContactoRepository extends JpaRepository<SolicitudContacto, Long> {

    List<SolicitudContacto> findByIdUsuarioReceptorOrderByFechaCreacionDesc(Long idUsuarioReceptor);

    List<SolicitudContacto> findByIdUsuarioEmisorOrderByFechaCreacionDesc(Long idUsuarioEmisor);

    Optional<SolicitudContacto> findByIdReporteAndIdUsuarioEmisorAndEstado(
            Long idReporte, Long idUsuarioEmisor, String estado);
}
