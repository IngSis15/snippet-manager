package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.RestController

@RestController
class ConfigRoutes
@Autowired
constructor(private val service: ConfigService) : ConfigRoutesSpec {

    private val logger: Logger = LoggerFactory.getLogger(ConfigRoutes::class.java)

    override fun getLintingConfig(jwt: Jwt): ResponseEntity<LintingSchemaDTO> {
        val userId = jwt.subject
        logger.info("Getting linting config for userId: $userId")
        val result = service.getLintingConfig(userId)
        logger.info("Returning linting config for userId: $userId")
        return ResponseEntity.ok(result)
    }

    override fun getFormattingConfig(jwt: Jwt): ResponseEntity<FormattingSchemaDTO> {
        val userId = jwt.subject
        logger.info("Getting formatting config for userId: $userId")
        val result = service.getFormattingConfig(userId)
        logger.info("Returning formatting config for userId: $userId")
        return ResponseEntity.ok(result)
    }

    override fun setLintingConfig(
        jwt: Jwt,
        config: LintingSchemaDTO,
    ): ResponseEntity<LintingSchemaDTO> {
        val userId = jwt.subject
        logger.info("Setting linting config for userId: $userId")
        val result = service.lintSnippets(jwt, config)
        logger.info("Linting config set for userId: $userId")
        return ResponseEntity.ok(result)
    }

    override fun setFormattingConfig(
        jwt: Jwt,
        config: FormattingSchemaDTO,
    ): ResponseEntity<FormattingSchemaDTO> {
        val userId = jwt.subject
        logger.info("Setting formatting config for userId: $userId")
        val result = service.formatSnippets(jwt, config)
        logger.info("Formatting config set for userId: $userId")
        return ResponseEntity.ok(result)
    }
}

