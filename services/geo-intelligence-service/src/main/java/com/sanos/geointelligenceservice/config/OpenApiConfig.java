package com.sanos.geointelligenceservice.config;

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
    public OpenAPI geoOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Geo-inteligencia (zonas) - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Zonas de incidencia y coordenadas asociadas a reportes. Base de datos: **db_geo**.

                                **Tablas:** `zonas_incidencia`, `coordenadas_reporte`.

                                **Autenticacion:** JWT (excepto `GET /api/zones/health`).
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
