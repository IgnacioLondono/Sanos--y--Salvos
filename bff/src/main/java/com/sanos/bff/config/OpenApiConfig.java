package com.sanos.bff.config;

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
    public OpenAPI bffOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("BFF (Backend for Frontend) - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Agregacion de lecturas para el portal: dashboard, mapa y vista de mascota.
                                No persiste datos propios; llama a los microservicios.

                                **Rutas:** `/api/bff/dashboard`, `/api/bff/map`, `/api/bff/pet-overview/{petId}`, `/api/bff/health`.

                                **Autenticacion:** JWT (excepto `GET /api/bff/health` publico en gateway).
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
