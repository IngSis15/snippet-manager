package edu.ingsis.snippetmanager.docs

import io.swagger.v3.oas.models.Components
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import io.swagger.v3.oas.models.security.SecurityRequirement
import io.swagger.v3.oas.models.security.SecurityScheme
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun customOpenAPI(): OpenAPI {
        val securitySchemeName = "BearerAuth"
        return OpenAPI()
            .components(
                Components().addSecuritySchemes(
                    securitySchemeName,
                    SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"),
                ),
            )
            .addSecurityItem(SecurityRequirement().addList(securitySchemeName))
            .info(
                Info().title("Snippet Manager API").version("v1"),
            ).servers(emptyList())
    }
}
