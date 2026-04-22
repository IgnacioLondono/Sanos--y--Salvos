package com.sanos.reportsservice.config;

import com.sanos.reportsservice.dto.ReportDto;
import com.sanos.reportsservice.repository.ReporteEventoRepository;
import com.sanos.reportsservice.service.ReportService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataSeeder implements CommandLineRunner {

    private final ReporteEventoRepository repo;
    private final ReportService service;

    public DataSeeder(ReporteEventoRepository repo, ReportService service) {
        this.repo = repo;
        this.service = service;
    }

    @Override
    public void run(String... args) {
        if (repo.count() > 0) return;

        service.create(new ReportDto(null, 1L, 2L, "PERDIDA", "ABIERTO",
                "Providencia", "Milo se escapo cerca del parque Bustamante",
                "Estable", new BigDecimal("-33.4413"), new BigDecimal("-70.6185"), null));

        service.create(new ReportDto(null, 2L, 2L, "ENCONTRADA", "ABIERTO",
                "Nunoa", "Gatita siamesa encontrada en Plaza Nunoa",
                "Hidratada, sin collar", new BigDecimal("-33.4563"), new BigDecimal("-70.5975"), null));

        service.create(new ReportDto(null, 3L, null, "PERDIDA", "ABIERTO",
                "Maipu", "Perro mestizo perdido en Maipu Centro",
                "Nervioso, sin collar", new BigDecimal("-33.5110"), new BigDecimal("-70.7580"), null));
    }
}
