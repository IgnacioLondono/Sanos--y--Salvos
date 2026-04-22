package com.sanos.iamservice.repository;

import com.sanos.iamservice.model.ContactoUsuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ContactoUsuarioRepository extends JpaRepository<ContactoUsuario, Long> {
    Optional<ContactoUsuario> findByCorreoElectronico(String correoElectronico);
    Optional<ContactoUsuario> findByIdUsuario(Long idUsuario);
}
