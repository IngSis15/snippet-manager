package edu.ingsis.snippetmanager.format

import edu.ingsis.snippetmanager.config.ConfigService
import edu.ingsis.snippetmanager.format.dto.FormatSnippetDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.System.getLogger

@Component
class FormatService
    @Autowired
    constructor(
        private val formatSnippetProducer: RedisFormatSnippetProducer,
        private val configService: ConfigService,
    ) {
        private val logger: System.Logger = getLogger(FormatService::class.simpleName)

        fun formatSnippet(
            snippetId: Long,
            userId: String,
        ) {

            try {
                saveDefaultConfig(userId)
                val sanitizedUserId = userId.replace("|", "")
                val formatSnippetDto = FormatSnippetDto(snippetId, sanitizedUserId)

                formatSnippetProducer.publishEvent(Json.encodeToString(formatSnippetDto))

                logger.log(System.Logger.Level.INFO, "Successfully published format event for snippetId: $snippetId")
            } catch (e: Exception) {
                logger.log(System.Logger.Level.ERROR, "Error while formatting snippet: ${e.message}", e)
                throw e
            }
        }

        private fun saveDefaultConfig(userId: String) {
            try {
                configService.getFormattingConfig(userId)
                logger.log(System.Logger.Level.INFO, "Default formatting config fetched successfully for userId: $userId")
            } catch (e: Exception) {
                logger.log(
                    System.Logger.Level.ERROR,
                    "Failed formatting config for userId: $userId, reason: ${e.message}",
                    e,
                )
                throw e
            }
        }
    }
