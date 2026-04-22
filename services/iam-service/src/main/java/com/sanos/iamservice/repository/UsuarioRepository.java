package com.sanos.iamservice.repository;

import com.sanos.iamservice.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByRutDocumento(String rutDocumento);
}
