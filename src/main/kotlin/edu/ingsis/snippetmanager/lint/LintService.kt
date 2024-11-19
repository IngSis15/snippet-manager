package edu.ingsis.snippetmanager.lint

import edu.ingsis.snippetmanager.config.ConfigService
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
        private val configService: ConfigService,
    ) {
        fun lintSnippet(
            snippetId: Long,
            userId: String,
        ) {
            saveDefaultConfig(userId)
            val lintSnippetDto = LintSnippetDto(snippetId, userId.replace("|", ""))
            lintSnippetProducer.publishEvent(Json.encodeToString(lintSnippetDto))
        }

        private fun saveDefaultConfig(userId: String) {
            configService.getLintingConfig(userId)
        }
    }
