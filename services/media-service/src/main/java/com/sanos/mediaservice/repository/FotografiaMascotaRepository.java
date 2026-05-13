package com.sanos.mediaservice.repository;

import com.sanos.mediaservice.model.FotografiaMascota;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FotografiaMascotaRepository extends JpaRepository<FotografiaMascota, Long> {
    List<FotografiaMascota> findByIdMascota(Long idMascota);
    List<FotografiaMascota> findByIdReporte(Long idReporte);
}
