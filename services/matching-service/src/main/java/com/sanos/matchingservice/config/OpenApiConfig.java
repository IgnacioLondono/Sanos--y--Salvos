package com.sanos.matchingservice.config;

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
    public OpenAPI matchingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Matching IA - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Coincidencias entre reportes de perdida y hallazgo. Base de datos: **db_matching**.

                                **Tablas:** `coincidencias_ia`, `desglose_similitud`.

                                El motor consulta reportes (y catalogo) vía HTTP interno.

                                **Autenticacion:** JWT (excepto `GET /api/matching/health`).
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
