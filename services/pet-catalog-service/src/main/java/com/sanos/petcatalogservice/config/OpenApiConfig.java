package com.sanos.petcatalogservice.config;

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
    public OpenAPI petCatalogOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Catalogo de mascotas - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Registro de mascotas y caracteristicas fisicas. Base de datos: **db_pets**.

                                **Tablas:** `mascotas`, `caracteristicas_fisicas`, `vinculos_mascotas`.

                                **Autenticacion:** JWT en `Authorization: Bearer <token>` (excepto `GET /api/pets/health` publico en gateway).
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
