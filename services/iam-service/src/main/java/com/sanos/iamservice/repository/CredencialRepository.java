package com.sanos.iamservice.repository;

import com.sanos.iamservice.model.Credencial;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CredencialRepository extends JpaRepository<Credencial, Long> {
    Optional<Credencial> findByIdUsuario(Long idUsuario);
}
