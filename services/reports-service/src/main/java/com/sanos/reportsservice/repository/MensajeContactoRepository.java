package com.sanos.reportsservice.repository;

import com.sanos.reportsservice.model.MensajeContacto;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {

    List<MensajeContacto> findByIdConversacionOrderByFechaCreacionAsc(Long idConversacion);
}
