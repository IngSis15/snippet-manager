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

        fun lintSnippets(
            jwt: Jwt,
            config: LintingSchemaDTO,
        ): LintingSchemaDTO {
            val userId = sanitizeUserId(jwt.subject)
            setLintingConfig(userId, config)

            permissionService.getAllOwnerSnippetPermissions(jwt).toIterable().map { snippet ->
                val lintSnippetDto = LintSnippetDto(snippet.snippetId, userId)
                lintSnippetProducer.publishEvent(json.encodeToString(lintSnippetDto))
            }

            return config
        }

        fun formatSnippets(
            jwt: Jwt,
            config: FormattingSchemaDTO,
        ): FormattingSchemaDTO {
            val userId = sanitizeUserId(jwt.subject)
            setFormattingConfig(userId, config)

            permissionService.getAllOwnerSnippetPermissions(jwt).toIterable().map { snippet ->
                val formatSnippetDto = FormatSnippetDto(snippet.snippetId, userId)
                formatSnippetProducer.publishEvent(json.encodeToString(formatSnippetDto))
            }

            return config
        }

        fun getLintingConfig(userId: String): LintingSchemaDTO {
            val sanitizedUserId = sanitizeUserId(userId)

            try {
                val config = assetService.getAsset("linting", sanitizedUserId).block()
                return json.decodeFromString<LintingSchemaDTO>(config!!)
            } catch (e: ResponseStatusException) {
                val defaultLintingConfig = ConfigFactory.defaultLintingRules()
                assetService.createAsset("linting", sanitizedUserId, defaultLintingConfig).block()
                return json.decodeFromString<LintingSchemaDTO>(defaultLintingConfig)
            }
        }

        fun getFormattingConfig(userId: String): FormattingSchemaDTO {
            val sanitizedUserId = sanitizeUserId(userId)
            try {
                val config = assetService.getAsset("formatting", sanitizedUserId).block()
                return json.decodeFromString<FormattingSchemaDTO>(config!!)
            } catch (e: ResponseStatusException) {
                val defaultFormattingRules = ConfigFactory.defaultFormattingRules()
                assetService.createAsset("formatting", sanitizedUserId, defaultFormattingRules).block()
                return json.decodeFromString<FormattingSchemaDTO>(defaultFormattingRules)
            }
        }

        fun setLintingConfig(
            userId: String,
            config: LintingSchemaDTO,
        ): LintingSchemaDTO {
            assetService.createAsset("linting", sanitizeUserId(userId), json.encodeToString(config)).block()
            return config
        }

        fun setFormattingConfig(
            userId: String,
            config: FormattingSchemaDTO,
        ): FormattingSchemaDTO {
            assetService.createAsset("formatting", sanitizeUserId(userId), json.encodeToString(config)).block()
            return config
        }

        private fun sanitizeUserId(userId: String): String {
            return userId.replace("|", "")
        }
    }
