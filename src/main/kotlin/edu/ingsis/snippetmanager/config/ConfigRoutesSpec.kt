package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.dto.FormattingConfigDto
import edu.ingsis.snippetmanager.config.dto.LintingConfigDto
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
    @GetMapping("/get/linting")
    @Operation(
        summary = "Get linting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
        ],
    )
    fun getLintingConfig(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<LintingConfigDto>

    @GetMapping("/get/formatting")
    @Operation(
        summary = "Get formatting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
        ],
    )
    fun getFormattingConfig(
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<FormattingConfigDto>

    @PutMapping("/set/linting")
    @Operation(
        summary = "Set linting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
        ],
    )
    fun setLintingConfig(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody config: LintingConfigDto,
    ): ResponseEntity<LintingConfigDto>

    @PutMapping("/set/formatting")
    @Operation(
        summary = "Set formatting configuration for user",
        parameters = [
            Parameter(name = "jwt", description = "JWT token", required = true),
            Parameter(name = "config", description = "Configuration object", required = true),
        ],
    )
    fun setFormattingConfig(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestBody config: FormattingConfigDto,
    ): ResponseEntity<FormattingConfigDto>
}
