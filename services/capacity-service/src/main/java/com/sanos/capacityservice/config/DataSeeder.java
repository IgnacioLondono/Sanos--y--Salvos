package com.sanos.capacityservice.config;

import com.sanos.capacityservice.model.EquipoColaboracion;
import com.sanos.capacityservice.repository.EquipoColaboracionRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EquipoColaboracionRepository repo;

    public DataSeeder(EquipoColaboracionRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        save("Brigada Centro",     "ONG Patitas Libres", "Santiago",    12, 40);
        save("Rescate Norte",      "Refugio Esperanza",  "Providencia",  8, 32);
        save("Equipo Poniente",    "Voluntarios Maipu",  "Maipu",       15, 60);
        save("Brigada Sur",        "Red Animalista",     "La Florida",   6, 24);
    }

    private void save(String nombre, String org, String zona, int voluntarios, int horas) {
        EquipoColaboracion e = new EquipoColaboracion();
        e.setNombreEquipo(nombre);
        e.setOrganizacion(org);
        e.setZonaOperacion(zona);
        e.setVoluntarios(voluntarios);
        e.setHorasDisponibles(horas);
        e.setDisponibleDesde(LocalDateTime.now());
        e.setFechaCreacion(LocalDateTime.now());
        repo.save(e);
    }
}
