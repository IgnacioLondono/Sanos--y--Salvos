package com.sanos.petcatalogservice.repository;

import com.sanos.petcatalogservice.model.VinculoMascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VinculoMascotaRepository extends JpaRepository<VinculoMascota, Long> {
    List<VinculoMascota> findByIdUsuario(Long idUsuario);
    List<VinculoMascota> findByIdMascota(Long idMascota);
}
