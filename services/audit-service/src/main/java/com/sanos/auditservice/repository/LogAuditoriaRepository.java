package com.sanos.auditservice.repository;

import com.sanos.auditservice.model.LogAuditoria;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LogAuditoriaRepository extends JpaRepository<LogAuditoria, Long> {
    List<LogAuditoria> findByEntidadIgnoreCase(String entidad);
    List<LogAuditoria> findByActorIgnoreCase(String actor);
}
