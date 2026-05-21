package com.sanos.forumservice.config;

import com.sanos.forumservice.dto.CreateThreadRequest;
import com.sanos.forumservice.repository.HiloForoRepository;
import com.sanos.forumservice.service.ForumService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seedForum(ForumService forumService, HiloForoRepository hiloRepo) {
        return args -> {
            if (hiloRepo.count() > 0) return;

            forumService.createThread(new CreateThreadRequest(
                    "Como publicar un reporte de mascota perdida?",
                    "Hola, soy nuevo en la plataforma. Quiero saber el paso a paso para registrar a mi mascota y marcar el punto en el mapa cuando la pierda.",
                    "AYUDA",
                    1L,
                    "Maria Lopez"
            ));

            forumService.createThread(new CreateThreadRequest(
                    "Consejos para fotos que ayuden en la busqueda",
                    "Comparto que conviene subir fotos con buena luz, collar visible y senas particulares. Eso ayudo mucho en mi caso.",
                    "CONSEJOS",
                    2L,
                    "Pedro Soto"
            ));

            forumService.createThread(new CreateThreadRequest(
                    "Refugio temporal en Providencia - cupos",
                    "Alguien conoce refugios o familias de acogida con cupo en Providencia? Tengo un perro encontrado y necesito derivarlo.",
                    "GENERAL",
                    1L,
                    "Ana Perez"
            ));
        };
    }
}
