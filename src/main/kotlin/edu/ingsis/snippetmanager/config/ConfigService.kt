package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import edu.ingsis.snippetmanager.config.models.FormattingConfig
import edu.ingsis.snippetmanager.config.models.LintingConfig
import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.lint.LintSnippetProducer
import edu.ingsis.snippetmanager.lint.dto.LintSnippetDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ConfigService
    @Autowired
    constructor(
        private val assetService: AssetApi,
        private val lintingConfigRepository: LintingConfigRepository,
        private val formattingConfigRepository: FormattingConfigRepository,
        private val permissionService: PermissionService,
        private val lintSnippetProducer: LintSnippetProducer,
    ) {
        private val json = Json { ignoreUnknownKeys = true }

        fun lintSnippets(
            jwt: Jwt,
            config: LintingSchemaDTO,
        ): LintingSchemaDTO {
            val userId = jwt.subject
            setLintingConfig(userId, config)
            val savedConfig = lintingConfigRepository.findConfigByUserId(userId)

            permissionService.getAllSnippetPermissions(jwt).toIterable().map { snippet ->
                val lintSnippetDto = LintSnippetDto(snippet.snippetId, savedConfig?.id!!)
                lintSnippetProducer.publishEvent(json.encodeToString(lintSnippetDto))
            }

            return config
        }

        fun getLintingConfig(userId: String): LintingSchemaDTO {
            val usersLintingEntity = lintingConfigRepository.findConfigByUserId(userId)
            if (usersLintingEntity != null) {
                val usersLintingConfig = fetchLintingConfigSpecs(usersLintingEntity.id.toString())
                return json.decodeFromString<LintingSchemaDTO>(usersLintingConfig)
            } else {
                val config = saveDefaultLintingConfig(userId)
                val content = assetService.getAsset("linting", config.id.toString()).block()
                return json.decodeFromString<LintingSchemaDTO>(content!!)
            }
        }

        fun getFormattingConfig(userId: String): FormattingSchemaDTO {
            val usersFormattingEntity = formattingConfigRepository.findConfigByUserId(userId)
            if (usersFormattingEntity != null) {
                val usersFormattingConfig = fetchFormattingConfigSpecs(usersFormattingEntity.id.toString())
                return json.decodeFromString<FormattingSchemaDTO>(usersFormattingConfig)
            } else {
                val config = saveDefaultFormattingConfig(userId)
                val content = assetService.getAsset("formatting", config.id.toString()).block()
                return json.decodeFromString<FormattingSchemaDTO>(content!!)
            }
        }

        fun setLintingConfig(
            userId: String,
            config: LintingSchemaDTO,
        ) {
            var usersLintingEntity = lintingConfigRepository.findConfigByUserId(userId)
            if (usersLintingEntity == null) {
                usersLintingEntity = lintingConfigRepository.save(LintingConfig(userId = userId))
            }
            assetService.createAsset("linting", usersLintingEntity.id.toString(), json.encodeToString(config)).block()
        }

        fun setFormattingConfig(
            userId: String,
            config: FormattingSchemaDTO,
        ): FormattingSchemaDTO {
            var usersFormattingEntity = formattingConfigRepository.findConfigByUserId(userId)
            if (usersFormattingEntity == null) {
                usersFormattingEntity = formattingConfigRepository.save(FormattingConfig(userId = userId))
            }
            assetService.createAsset("formatting", usersFormattingEntity.id.toString(), json.encodeToString(config)).block()
            return config
        }

        fun saveDefaultLintingConfig(userId: String): LintingConfig {
            val defaultLintingConfig = ConfigFactory.defaultLintingRules()
            val configSaved = lintingConfigRepository.save(LintingConfig(userId = userId))
            assetService.createAsset("linting", configSaved.id.toString(), defaultLintingConfig).block()
            return configSaved
        }

        fun saveDefaultFormattingConfig(userId: String): FormattingConfig {
            val defaultFormattingConfig = ConfigFactory.defaultFormattingRules()
            val configSaved = formattingConfigRepository.save(FormattingConfig(userId = userId))
            assetService.createAsset("formatting", configSaved.id.toString(), defaultFormattingConfig).block()
            return configSaved
        }

        fun fetchLintingConfigSpecs(configId: String): String {
            return assetService.getAsset("linting", configId).block()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Linting config not found")
        }

        fun fetchFormattingConfigSpecs(configId: String): String {
            return assetService.getAsset("formatting", configId).block()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Formatting config not found")
        }
    }
