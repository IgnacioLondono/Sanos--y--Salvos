package com.sanos.forumservice.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI forumOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Foro comunitario - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Hilos y mensajes de ayuda entre ciudadanos. Base de datos: **db_foro** (database per service).

                                **Tablas:**
                                - `hilos_foro` — titulo, categoria, autor, fechas.
                                - `mensajes_foro` — contenido de cada mensaje/respuesta vinculado a un hilo.

                                **Categorias:** `AYUDA`, `CONSEJOS`, `GENERAL`.

                                **Autenticacion:** `GET` publico via gateway; `POST` requiere JWT Bearer (obtener en `POST /api/iam/login`).

                                **Puerto directo:** 8099 | **Via gateway:** `/api/forum/**`
                                """)
                        .license(new License().name("Sanos y Salvos").url("https://github.com/IgnacioLondono/Sanos--y--Salvos")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Token JWT de POST /api/iam/login")));
    }
}
