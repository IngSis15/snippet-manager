package edu.ingsis.snippetmanager.format

import edu.ingsis.snippetmanager.config.ConfigService
import edu.ingsis.snippetmanager.format.dto.FormatSnippetDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component

@Component
class FormatService
    @Autowired
    constructor(
        private val formatSnippetProducer: RedisFormatSnippetProducer,
        private val configService: ConfigService,
    ) {
        fun formatSnippet(
            snippetId: Long,
            userId: String,
        ) {
            saveDefaultConfig(userId)
            val formatSnippetDto = FormatSnippetDto(snippetId, userId.replace("|", ""))
            formatSnippetProducer.publishEvent(Json.encodeToString(formatSnippetDto))
        }

        private fun saveDefaultConfig(userId: String) {
            configService.getFormattingConfig(userId)
        }
    }
