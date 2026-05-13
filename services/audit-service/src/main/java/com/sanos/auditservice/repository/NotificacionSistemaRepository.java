package com.sanos.auditservice.repository;

import com.sanos.auditservice.model.NotificacionSistema;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificacionSistemaRepository extends JpaRepository<NotificacionSistema, Long> {
    List<NotificacionSistema> findByIdUsuarioDestino(Long idUsuarioDestino);
}
