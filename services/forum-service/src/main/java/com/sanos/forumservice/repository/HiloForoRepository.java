package com.sanos.forumservice.repository;

import com.sanos.forumservice.model.HiloForo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HiloForoRepository extends JpaRepository<HiloForo, Long> {
    List<HiloForo> findAllByOrderByFechaActualizacionDesc();
    List<HiloForo> findByCategoriaIgnoreCaseOrderByFechaActualizacionDesc(String categoria);
}
