package edu.ingsis.snippetmanager.lint

import edu.ingsis.snippetmanager.config.ConfigService
import edu.ingsis.snippetmanager.lint.dto.LintSnippetDto
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.lang.System.getLogger

@Component
class LintService
    @Autowired
    constructor(
        private val lintSnippetProducer: LintSnippetProducer,
        private val configService: ConfigService,
    ) {
        private val logger: System.Logger = getLogger(LintService::class.simpleName)

        fun lintSnippet(
            snippetId: Long,
            userId: String,
        ) {
            logger.log(System.Logger.Level.INFO, "Starting linting process for snippetId: $snippetId, userId: $userId")
            try {
                saveDefaultConfig(userId)
                val lintSnippetDto = LintSnippetDto(snippetId, userId.replace("|", ""))
                lintSnippetProducer.publishEvent(Json.encodeToString(lintSnippetDto))
                logger.log(System.Logger.Level.INFO, "Linting process completed for snippetId: $snippetId")
            } catch (e: Exception) {
                logger.log(System.Logger.Level.ERROR, "Error during linting process for snippetId: $snippetId, userId: $userId", e)
                throw e
            }
        }

        private fun saveDefaultConfig(userId: String) {
            configService.getLintingConfig(userId)
        }
    }
