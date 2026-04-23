package com.sanos.auditservice.config;

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
    public OpenAPI auditOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Auditoria y notificaciones - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Trazabilidad de cambios y notificaciones de sistema. Base de datos: **db_audit**.

                                **Tablas:** `log_auditoria`, `notificaciones_sistema`.

                                **Autenticacion:** JWT (excepto `GET /api/audit/health`).
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
