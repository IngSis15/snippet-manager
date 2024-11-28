package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.format.FormatSnippetProducer
import edu.ingsis.snippetmanager.format.dto.FormatSnippetDto
import edu.ingsis.snippetmanager.lint.LintSnippetProducer
import edu.ingsis.snippetmanager.lint.dto.LintSnippetDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ConfigService
    @Autowired
    constructor(
        private val assetService: AssetApi,
        private val permissionService: PermissionService,
        private val lintSnippetProducer: LintSnippetProducer,
        private val formatSnippetProducer: FormatSnippetProducer,
    ) {
        private val json = Json { ignoreUnknownKeys = true }

        private val logger: Logger = LoggerFactory.getLogger(ConfigService::class.java)

        fun lintSnippets(
            jwt: Jwt,
            config: LintingSchemaDTO,
        ): LintingSchemaDTO {
            val userId = sanitizeUserId(jwt.subject)
            try {
                setLintingConfig(userId, config)

                permissionService.getAllOwnerSnippetPermissions(jwt).toIterable().map { snippet ->
                    val lintSnippetDto = LintSnippetDto(snippet.snippetId, userId)
                    lintSnippetProducer.publishEvent(json.encodeToString(lintSnippetDto))
                    logger.debug("Published lint snippet event for snippetId: ${snippet.snippetId}")
                }
                return config
            } catch (e: Exception) {
                logger.error("Error while linting snippets for userId: $userId", e)
                throw e
            }
        }

        fun formatSnippets(
            jwt: Jwt,
            config: FormattingSchemaDTO,
        ): FormattingSchemaDTO {
            val userId = sanitizeUserId(jwt.subject)
            try {
                setFormattingConfig(userId, config)

                permissionService.getAllOwnerSnippetPermissions(jwt).toIterable().map { snippet ->
                    val formatSnippetDto = FormatSnippetDto(snippet.snippetId, userId)
                    formatSnippetProducer.publishEvent(json.encodeToString(formatSnippetDto))
                    logger.debug("Published format snippet event for snippetId: ${snippet.snippetId}")
                }
                return config
            } catch (e: Exception) {
                logger.error("Error while formatting snippets for userId: $userId", e)
                throw e
            }
        }

        fun getLintingConfig(userId: String): LintingSchemaDTO {
            val correlationId = MDC.get("correlation-id")
            val sanitizedUserId = sanitizeUserId(userId)
            try {
                val config = assetService.getAsset("linting", sanitizedUserId, correlationId).block()
                if (config == null) {
                    logger.info("No existing linting config found, creating default config for userId: $sanitizedUserId")
                    val defaultConfig = ConfigFactory.defaultLintingRules()
                    assetService.createAsset("linting", sanitizedUserId, defaultConfig, correlationId).block()
                    return json.decodeFromString(defaultConfig)
                } else {
                    return json.decodeFromString(config)
                }
            } catch (e: ResponseStatusException) {
                logger.error("Error fetching linting config for userId: $sanitizedUserId", e)
                val defaultConfig = ConfigFactory.defaultLintingRules()
                assetService.createAsset("linting", sanitizedUserId, defaultConfig, correlationId).block()
                return json.decodeFromString(defaultConfig)
            }
        }

        fun getFormattingConfig(userId: String): FormattingSchemaDTO {
            val correlationId = MDC.get("correlation-id")
            val sanitizedUserId = sanitizeUserId(userId)
            try {
                val config = assetService.getAsset("formatting", sanitizedUserId, correlationId).block()
                if (config == null) {
                    logger.info("No existing formatting config found, creating default config for userId: $sanitizedUserId")
                    val defaultConfig = ConfigFactory.defaultFormattingRules()
                    assetService.createAsset("formatting", sanitizedUserId, defaultConfig, correlationId).block()
                    return json.decodeFromString(defaultConfig)
                } else {
                    return json.decodeFromString(config)
                }
            } catch (e: ResponseStatusException) {
                logger.error("Error fetching formatting config for userId: $sanitizedUserId", e)
                val defaultConfig = ConfigFactory.defaultFormattingRules()
                assetService.createAsset("formatting", sanitizedUserId, defaultConfig, correlationId).block()
                return json.decodeFromString(defaultConfig)
            }
        }

        fun setLintingConfig(
            userId: String,
            config: LintingSchemaDTO,
        ): LintingSchemaDTO {
            val correlationId = MDC.get("correlation-id")
            assetService.createAsset("linting", sanitizeUserId(userId), json.encodeToString(config), correlationId).block()
            return config
        }

        fun setFormattingConfig(
            userId: String,
            config: FormattingSchemaDTO,
        ): FormattingSchemaDTO {
            val correlationId = MDC.get("correlation-id")
            assetService.createAsset("formatting", sanitizeUserId(userId), json.encodeToString(config), correlationId).block()
            return config
        }

        private fun sanitizeUserId(userId: String): String {
            return userId.replace("|", "")
        }
    }
