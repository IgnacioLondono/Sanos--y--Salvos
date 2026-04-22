package com.sanos.matchingservice.repository;

import com.sanos.matchingservice.model.DesgloseSimilitud;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DesgloseSimilitudRepository extends JpaRepository<DesgloseSimilitud, Long> {
    List<DesgloseSimilitud> findByIdMatch(Long idMatch);
}
