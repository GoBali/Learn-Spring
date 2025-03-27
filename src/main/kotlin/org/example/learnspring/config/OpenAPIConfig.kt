package org.example.learnspring.config

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenAPIConfig {

    @Bean
    fun api(): OpenAPI {
        val securityScheme = SecurityScheme()
            .type(SecurityScheme.Type.HTTP)
            .scheme("bearer")
            .bearerFormat("JWT")
            .name("Authorization")
            .`in`(SecurityScheme.In.HEADER)

        val securityRequirement = SecurityRequirement().addList("BearerAuth")

        return OpenAPI()
            .components(Components().addSecuritySchemes("BearerAuth", securityScheme))
            .addSecurityItem(securityRequirement)
            .info(Info().title("API 문서").description("설명").version("v1"))
    }
}