package com.sanos.mediaservice.config;

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
    public OpenAPI mediaOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Media (fotografias) - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Evidencia fotografica ligada a mascotas y reportes. Base de datos: **db_media**.

                                **Tablas:** `fotografias_mascotas`.

                                **Autenticacion:** JWT (excepto `GET /api/media/health`).
                                """)
                        .license(new License().name("Sanos y Salvos").url("https://github.com/IgnacioLondono/Sanos--y--Salvos")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")));
    }
}
