package edu.ingsis.snippetmanager.lint

import edu.ingsis.snippetmanager.config.ConfigService
import edu.ingsis.snippetmanager.config.LintingConfigRepository
import edu.ingsis.snippetmanager.lint.dto.LintSnippetDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class LintService
    @Autowired
    constructor(
        private val lintSnippetProducer: LintSnippetProducer,
        private val configRepository: LintingConfigRepository,
        private val configService: ConfigService,
    ) {
        fun lintSnippet(
            snippetId: Long,
            userId: String,
        ) {
            val configId =
                configRepository.findConfigByUserId(userId)?.id
                    ?: configService.saveDefaultLintingConfig(userId).id

            val lintSnippetDto = LintSnippetDto(snippetId, configId!!)

            lintSnippetProducer.publishEvent(Json.encodeToString(lintSnippetDto))
        }
    }
