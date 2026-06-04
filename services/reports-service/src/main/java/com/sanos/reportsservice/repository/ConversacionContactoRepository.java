package com.sanos.reportsservice.repository;

import com.sanos.reportsservice.model.ConversacionContacto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ConversacionContactoRepository extends JpaRepository<ConversacionContacto, Long> {

    Optional<ConversacionContacto> findByIdSolicitud(Long idSolicitud);

    @Query("""
            SELECT c FROM ConversacionContacto c
            WHERE (c.idUsuarioEmisor = :userId OR c.idUsuarioReceptor = :userId)
              AND (:estado IS NULL OR c.estado = :estado)
            ORDER BY c.fechaCreacion DESC
            """)
    List<ConversacionContacto> findForParticipant(
            @Param("userId") Long userId,
            @Param("estado") String estado);
}
