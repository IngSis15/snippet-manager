package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("v1/config")
interface ConfigRoutesSpec {
    @GetMapping("/linting")
    @Operation(
        summary = "Get linting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
        ],
    )
    fun getLintingConfig(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<LintingSchemaDTO>

    @GetMapping("/formatting")
    @Operation(
        summary = "Get formatting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
        ],
    )
    fun getFormattingConfig(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<FormattingSchemaDTO>

    @PutMapping("/linting")
    @Operation(
        summary = "Set linting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
        ],
    )
    fun setLintingConfig(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody config: LintingSchemaDTO,
    ): ResponseEntity<LintingSchemaDTO>

    @PutMapping("/formatting")
    @Operation(
        summary = "Set formatting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
            Parameter(name = "config", description = "Configuration object", required = true),
        ],
    )
    fun setFormattingConfig(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody config: FormattingSchemaDTO,
    ): ResponseEntity<FormattingSchemaDTO>
}
