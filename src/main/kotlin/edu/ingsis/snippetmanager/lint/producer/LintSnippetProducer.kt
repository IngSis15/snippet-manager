package edu.ingsis.snippetmanager.lint.producer

import edu.ingsis.snippetmanager.redis.config.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.lang.System.getLogger

interface LintSnippetProducer {
    fun publishEvent(event: String)
}

@Component
class RedisLintSnippetProducer
    @Autowired
    constructor(
        @Value("\${stream.lint.key}") streamKey: String,
        redis: RedisTemplate<String, String>,
    ) : LintSnippetProducer, RedisStreamProducer(streamKey, redis) {
        val logger: System.Logger = getLogger(LintSnippetProducer::class.simpleName)

        override fun publishEvent(event: String) {
            logger.log(System.Logger.Level.INFO, "Linting snippet: $event")
            emit(event)
        }
    }
