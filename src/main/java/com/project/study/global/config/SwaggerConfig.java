package com.project.study.global.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {
    private final String JWT = "JWT";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .addSecurityItem(new SecurityRequirement().addList(JWT))
                .components(new Components().addSecuritySchemes(JWT, securityScheme()))
                .info(apiInfo());
    }

    private SecurityScheme securityScheme() {
        return new SecurityScheme()
                .type(SecurityScheme.Type.HTTP)
                .bearerFormat(JWT)
                .scheme("bearer");
    }

    private Info apiInfo() {
        return new Info()
                .title("StudyRoom")
                .description("StudyRoom Service API")
                .version("0.0.1");
    }
}