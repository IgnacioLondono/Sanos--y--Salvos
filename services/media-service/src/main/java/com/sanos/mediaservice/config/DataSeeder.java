package com.sanos.mediaservice.config;

import com.sanos.mediaservice.model.FotografiaMascota;
import com.sanos.mediaservice.repository.FotografiaMascotaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final FotografiaMascotaRepository repo;

    public DataSeeder(FotografiaMascotaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        save(1L, 1L, "https://placehold.co/600x400?text=Milo", "milo,perdida,parque");
        save(2L, 2L, "https://placehold.co/600x400?text=Luna", "luna,siames,hallazgo");
        save(3L, 3L, "https://placehold.co/600x400?text=Rocky", "rocky,maipu");
    }

    private void save(Long petId, Long reportId, String url, String tags) {
        FotografiaMascota f = new FotografiaMascota();
        f.setIdMascota(petId);
        f.setIdReporte(reportId);
        f.setUrlAlmacenamiento(url);
        f.setTags(tags);
        f.setFechaCaptura(LocalDateTime.now());
        repo.save(f);
    }
}
