package com.sanos.iamservice.config;

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
    public OpenAPI iamOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("IAM - Sanos y Salvos")
                        .version("1.0.0")
                        .description("""
                                Identidad y acceso. Base de datos: **db_iam**.

                                **Tablas (dominio):** `usuarios`, `credenciales`, `contactos_usuario`.

                                **Autenticacion:** la mayoria de rutas requieren `Authorization: Bearer <JWT>`.
                                Rutas publicas a traves del gateway: `POST /api/iam/register`, `POST /api/iam/login`, `GET /api/iam/health`.
                                """)
                        .license(new License().name("Sanos y Salvos").url("https://github.com/IgnacioLondono/Sanos--y--Salvos")))
                .components(new Components()
                        .addSecuritySchemes("bearer-jwt",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.HTTP)
                                        .scheme("bearer")
                                        .bearerFormat("JWT")
                                        .description("Obtener token con POST /api/iam/login (email + password).")));
    }
}
