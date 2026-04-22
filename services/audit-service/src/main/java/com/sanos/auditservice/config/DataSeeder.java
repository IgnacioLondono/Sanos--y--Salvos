package com.sanos.auditservice.config;

import com.sanos.auditservice.model.LogAuditoria;
import com.sanos.auditservice.repository.LogAuditoriaRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final LogAuditoriaRepository repo;

    public DataSeeder(LogAuditoriaRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;
        log("usuarios",     "CREATE", "system@sanosysalvos.cl", "{\"mensaje\":\"seed admin\"}");
        log("mascotas",     "CREATE", "system@sanosysalvos.cl", "{\"mensaje\":\"seed mascotas\"}");
        log("reportes",     "CREATE", "demo@sanosysalvos.cl",   "{\"mensaje\":\"seed reportes\"}");
        log("zonas",        "CREATE", "system@sanosysalvos.cl", "{\"mensaje\":\"seed zonas\"}");
    }

    private void log(String entity, String op, String actor, String changes) {
        LogAuditoria l = new LogAuditoria();
        l.setEntidad(entity);
        l.setTablaAfectada(entity);
        l.setOperacion(op);
        l.setAccionRealizada(op);
        l.setActor(actor);
        l.setCambiosJson(changes);
        l.setCreadoEn(LocalDateTime.now());
        repo.save(l);
    }
}
