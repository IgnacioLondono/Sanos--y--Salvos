package com.sanos.reportsservice.config;

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
    public OpenAPI reportsOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Reportes de eventos - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Reportes de perdida/hallazgo con ubicacion. Base de datos: **db_reports**.

                                **Tablas:** `reportes_eventos`, `detalles_reporte`.

                                **Autenticacion:** JWT en `Authorization: Bearer <token>` (excepto `GET /api/reports/health`).
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
