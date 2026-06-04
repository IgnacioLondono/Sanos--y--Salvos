package com.sanos.petcatalogservice.service;

import com.sanos.petcatalogservice.dto.PetDto;
import com.sanos.petcatalogservice.model.CaracteristicaFisica;
import com.sanos.petcatalogservice.model.Mascota;
import com.sanos.petcatalogservice.model.VinculoMascota;
import com.sanos.petcatalogservice.repository.CaracteristicaFisicaRepository;
import com.sanos.petcatalogservice.repository.MascotaRepository;
import com.sanos.petcatalogservice.repository.VinculoMascotaRepository;
import com.sanos.petcatalogservice.util.ApiDateTimes;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class PetService {

    private final MascotaRepository mascotaRepo;
    private final CaracteristicaFisicaRepository caracteristicaRepo;
    private final VinculoMascotaRepository vinculoRepo;

    public PetService(MascotaRepository mascotaRepo,
                      CaracteristicaFisicaRepository caracteristicaRepo,
                      VinculoMascotaRepository vinculoRepo) {
        this.mascotaRepo = mascotaRepo;
        this.caracteristicaRepo = caracteristicaRepo;
        this.vinculoRepo = vinculoRepo;
    }

    @Transactional(readOnly = true)
    public List<PetDto> listAll() {
        return mascotaRepo.findAll().stream().map(this::toDto).toList();
    }

    @Transactional(readOnly = true)
    public Optional<PetDto> findById(Long id) {
        return mascotaRepo.findById(id).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public Optional<PetDto> findByChip(String chip) {
        return mascotaRepo.findByNumeroChip(chip).map(this::toDto);
    }

    @Transactional(readOnly = true)
    public List<PetDto> findByOwner(Long ownerId) {
        return vinculoRepo.findByIdUsuario(ownerId).stream()
                .map(v -> mascotaRepo.findById(v.getIdMascota()).orElse(null))
                .filter(java.util.Objects::nonNull)
                .map(this::toDto)
                .toList();
    }

    @Transactional
    public PetDto create(PetDto req) {
        Mascota mascota = new Mascota();
        mascota.setNombre(req.name());
        mascota.setEspecie(req.species());
        mascota.setNumeroChip(req.chipNumber());
        mascota.setFechaRegistro(LocalDateTime.now());
        mascota = mascotaRepo.save(mascota);

        CaracteristicaFisica caract = new CaracteristicaFisica();
        caract.setIdMascota(mascota.getIdMascota());
        caract.setRaza(req.breed());
        caract.setColorPrincipal(req.color());
        caract.setTamano(req.size());
        caracteristicaRepo.save(caract);

        if (req.ownerId() != null) {
            VinculoMascota vin = new VinculoMascota();
            vin.setIdMascota(mascota.getIdMascota());
            vin.setIdUsuario(req.ownerId());
            vin.setTipoRelacion("DUENO");
            vinculoRepo.save(vin);
        }

        return toDto(mascota);
    }

    @Transactional
    public void delete(Long id) {
        caracteristicaRepo.findByIdMascota(id).ifPresent(caracteristicaRepo::delete);
        vinculoRepo.findByIdMascota(id).forEach(vinculoRepo::delete);
        mascotaRepo.deleteById(id);
    }

    public PetDto toDto(Mascota m) {
        CaracteristicaFisica caract = caracteristicaRepo.findByIdMascota(m.getIdMascota()).orElse(null);
        Long ownerId = vinculoRepo.findByIdMascota(m.getIdMascota()).stream()
                .findFirst().map(VinculoMascota::getIdUsuario).orElse(null);

        return new PetDto(
                m.getIdMascota(),
                m.getNombre(),
                m.getEspecie(),
                caract != null ? caract.getRaza() : null,
                caract != null ? caract.getColorPrincipal() : null,
                caract != null ? caract.getTamano() : null,
                m.getNumeroChip(),
                ownerId,
                ApiDateTimes.format(m.getFechaRegistro())
        );
    }
}
