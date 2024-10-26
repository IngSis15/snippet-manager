package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.dto.FormattingConfigDto
import edu.ingsis.snippetmanager.config.dto.LintingConfigDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigRoutes
    @Autowired
    constructor(private val service: ConfigService) : ConfigRoutesSpec {
        override fun getLintingConfig(jwt: Jwt): ResponseEntity<LintingConfigDto> {
            val userId = jwt.subject
            return ResponseEntity.ok(service.getLintingConfig(userId))
        }

        override fun getFormattingConfig(jwt: Jwt): ResponseEntity<FormattingConfigDto> {
            val userId = jwt.subject
            return ResponseEntity.ok(service.getFormattingConfig(userId))
        }

        override fun setLintingConfig(
            jwt: Jwt,
            config: LintingConfigDto,
        ): ResponseEntity<LintingConfigDto> {
            val userId = jwt.subject
            return ResponseEntity.ok(service.setLintingConfig(userId, config))
        }

        override fun setFormattingConfig(
            jwt: Jwt,
            config: FormattingConfigDto,
        ): ResponseEntity<FormattingConfigDto> {
            val userId = jwt.subject
            return ResponseEntity.ok(service.setFormattingConfig(userId, config))
        }
    }
