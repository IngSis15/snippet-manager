package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigRoutes
    @Autowired
    constructor(private val service: ConfigService) : ConfigRoutesSpec {
        override fun getLintingConfig(jwt: Jwt): ResponseEntity<LintingSchemaDTO> {
            val userId = jwt.subject
            return ResponseEntity.ok(service.getLintingConfig(userId))
        }

        override fun getFormattingConfig(jwt: Jwt): ResponseEntity<FormattingSchemaDTO> {
            val userId = jwt.subject
            return ResponseEntity.ok(service.getFormattingConfig(userId))
        }

        override fun setLintingConfig(
            jwt: Jwt,
            config: LintingSchemaDTO,
        ): ResponseEntity<LintingSchemaDTO> {
            val updatedConfig = service.lintSnippets(jwt, config)
            return ResponseEntity.ok(updatedConfig)
        }

        override fun setFormattingConfig(
            jwt: Jwt,
            config: FormattingSchemaDTO,
        ): ResponseEntity<FormattingSchemaDTO> {
            val userId = jwt.subject
            return ResponseEntity.ok(service.setFormattingConfig(userId, config))
        }
    }
