package com.sanos.forumservice.repository;

import com.sanos.forumservice.model.MensajeForo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface MensajeForoRepository extends JpaRepository<MensajeForo, Long> {
    List<MensajeForo> findByIdHiloOrderByFechaCreacionAsc(Long idHilo);
    long countByIdHilo(Long idHilo);
    Optional<MensajeForo> findFirstByIdHiloOrderByFechaCreacionAsc(Long idHilo);
}
