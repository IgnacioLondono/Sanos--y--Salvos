package com.sanos.petcatalogservice.service;

import com.sanos.petcatalogservice.dto.PetDto;
import com.sanos.petcatalogservice.model.CaracteristicaFisica;
import com.sanos.petcatalogservice.model.Mascota;
import com.sanos.petcatalogservice.model.VinculoMascota;
import com.sanos.petcatalogservice.repository.CaracteristicaFisicaRepository;
import com.sanos.petcatalogservice.repository.MascotaRepository;
import com.sanos.petcatalogservice.repository.VinculoMascotaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PetServiceTest {

    @Mock
    private MascotaRepository mascotaRepo;
    @Mock
    private CaracteristicaFisicaRepository caracteristicaRepo;
    @Mock
    private VinculoMascotaRepository vinculoRepo;

    private PetService service;

    @BeforeEach
    void setUp() {
        service = new PetService(mascotaRepo, caracteristicaRepo, vinculoRepo);
    }

    @Test
    void create_withOwner_savesVinculo() {
        PetDto req = new PetDto(null, "Milo", "DOG", "Mestizo", "Cafe", "M", "CH-1", 88L, null);
        when(mascotaRepo.save(any(Mascota.class))).thenAnswer(inv -> {
            Mascota m = inv.getArgument(0);
            m.setIdMascota(5L);
            return m;
        });
        when(vinculoRepo.findByIdMascota(5L)).thenReturn(List.of(vinculo(88L, 5L)));

        PetDto created = service.create(req);

        assertEquals(5L, created.id());
        assertEquals(88L, created.ownerId());
        verify(vinculoRepo).save(any(VinculoMascota.class));
    }

    @Test
    void create_withoutOwner_doesNotSaveVinculo() {
        PetDto req = new PetDto(null, "Luna", "CAT", "Siames", "Gris", "S", "CH-2", null, null);
        when(mascotaRepo.save(any(Mascota.class))).thenAnswer(inv -> {
            Mascota m = inv.getArgument(0);
            m.setIdMascota(6L);
            return m;
        });
        when(vinculoRepo.findByIdMascota(6L)).thenReturn(List.of());

        PetDto created = service.create(req);

        assertNull(created.ownerId());
        verify(vinculoRepo, never()).save(any(VinculoMascota.class));
    }

    @Test
    void findByOwner_returnsOnlyExistingPets() {
        VinculoMascota v1 = vinculo(99L, 1L);
        VinculoMascota v2 = vinculo(99L, 2L);
        when(vinculoRepo.findByIdUsuario(99L)).thenReturn(List.of(v1, v2));
        when(mascotaRepo.findById(1L)).thenReturn(Optional.of(mascota(1L, "A")));
        when(mascotaRepo.findById(2L)).thenReturn(Optional.empty());
        when(caracteristicaRepo.findByIdMascota(1L)).thenReturn(Optional.empty());
        when(vinculoRepo.findByIdMascota(1L)).thenReturn(List.of(v1));

        List<PetDto> result = service.findByOwner(99L);

        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).id());
    }

    @Test
    void delete_removesDetailRelationsBeforePet() {
        when(caracteristicaRepo.findByIdMascota(7L)).thenReturn(Optional.of(new CaracteristicaFisica()));
        when(vinculoRepo.findByIdMascota(7L)).thenReturn(List.of(vinculo(1L, 7L), vinculo(2L, 7L)));

        service.delete(7L);

        verify(caracteristicaRepo).delete(any(CaracteristicaFisica.class));
        verify(vinculoRepo, times(2)).delete(any(VinculoMascota.class));
        verify(mascotaRepo).deleteById(7L);
    }

    @Test
    void toDto_handlesMissingCaracteristicaAndOwner() {
        Mascota m = mascota(33L, "Rocky");
        when(caracteristicaRepo.findByIdMascota(33L)).thenReturn(Optional.empty());
        when(vinculoRepo.findByIdMascota(33L)).thenReturn(List.of());

        PetDto dto = service.toDto(m);

        assertEquals("Rocky", dto.name());
        assertNull(dto.breed());
        assertNull(dto.ownerId());
    }

    private Mascota mascota(Long id, String name) {
        Mascota m = new Mascota();
        m.setIdMascota(id);
        m.setNombre(name);
        m.setEspecie("DOG");
        m.setNumeroChip("CH-" + id);
        m.setFechaRegistro(LocalDateTime.now());
        return m;
    }

    private VinculoMascota vinculo(Long userId, Long petId) {
        VinculoMascota v = new VinculoMascota();
        v.setIdUsuario(userId);
        v.setIdMascota(petId);
        v.setTipoRelacion("DUENO");
        return v;
    }
}
