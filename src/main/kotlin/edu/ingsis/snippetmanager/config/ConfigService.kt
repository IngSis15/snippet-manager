package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import edu.ingsis.snippetmanager.external.asset.AssetApi
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class ConfigService
    @Autowired
    constructor(
        private val assetService: AssetApi,
        private val lintingConfigRepository: LintingConfigRepository,
        private val formattingConfigRepository: FormattingConfigRepository,
    ) {
        private val json = Json { ignoreUnknownKeys = true }

        fun getLintingConfig(userId: String): LintingSchemaDTO {
            val usersLintingEntity = lintingConfigRepository.findConfigByUserId(userId)
            if (usersLintingEntity != null) {
                val usersLintingConfig = fetchLintingConfigSpecs(usersLintingEntity.id.toString())
                return json.decodeFromString<LintingSchemaDTO>(usersLintingConfig)
            } else {
                val defaultLintingConfig = """
                {
                  "identifier_format": "camel case",
                  "mandatory-variable-or-literal-in-println": true,
                  "mandatory-variable-or-literal-in-readInput": true
                }
                """
                val idGetter = lintingConfigRepository.save(LintingConfig(userId = userId))
                assetService.createAsset("linting", idGetter.id.toString(), defaultLintingConfig).block()
                return json.decodeFromString<LintingSchemaDTO>(defaultLintingConfig)
            }
        }

        fun getFormattingConfig(userId: String): FormattingSchemaDTO {
            val usersFormattingEntity = formattingConfigRepository.findConfigByUserId(userId)
            if (usersFormattingEntity != null) {
                val usersFormattingConfig = fetchFormattingConfigSpecs(usersFormattingEntity.id.toString())
                return json.decodeFromString<FormattingSchemaDTO>(usersFormattingConfig)
            } else {
                val defaultFormattingConfig = """
                {
                  "enforce-spacing-before-colon-in-declaration": false,
                  "enforce-spacing-after-colon-in-declaration": false,
                  "enforce-no-spacing-around-equals": true,
                  "newLinesBeforePrintln": 0,
                  "indent-inside-if": 4
                }
                """
                val idGetter = formattingConfigRepository.save(FormattingConfig(userId = userId))
                assetService.createAsset("formatting", idGetter.id.toString(), defaultFormattingConfig).block()
                return json.decodeFromString<FormattingSchemaDTO>(defaultFormattingConfig)
            }
        }

        fun setLintingConfig(
            userId: String,
            config: LintingSchemaDTO,
        ): LintingSchemaDTO {
            var usersLintingEntity = lintingConfigRepository.findConfigByUserId(userId)
            if (usersLintingEntity == null) {
                usersLintingEntity = lintingConfigRepository.save(LintingConfig(userId = userId))
            }
            assetService.createAsset("linting", usersLintingEntity.id.toString(), json.encodeToString(config)).block()
            return config
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

        fun fetchLintingConfigSpecs(configId: String): String {
            return assetService.getAsset("linting", configId).block()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Linting config not found")
        }

        fun fetchFormattingConfigSpecs(configId: String): String {
            return assetService.getAsset("formatting", configId).block()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Formatting config not found")
        }
    }
