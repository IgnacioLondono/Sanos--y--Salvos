package com.sanos.capacityservice.config;

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
    public OpenAPI capacityOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Capacidad y colaboracion - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Equipos de rescate y asignacion de horas. Base de datos: **db_capacity**.

                                **Tablas:** `equipos_colaboracion`, `asignacion_capacidad`.

                                **Autenticacion:** JWT (excepto `GET /api/capacity/health`).
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
