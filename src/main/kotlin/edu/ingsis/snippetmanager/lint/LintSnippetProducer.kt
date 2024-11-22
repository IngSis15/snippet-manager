package edu.ingsis.snippetmanager.lint

import org.austral.ingsis.redis.RedisStreamProducer
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
        logger.log(System.Logger.Level.INFO, "Publishing lint event to Redis stream: $event")
        try {
            emit(event)
            logger.log(System.Logger.Level.INFO, "Successfully published lint event: $event")
        } catch (e: Exception) {
            logger.log(System.Logger.Level.ERROR, "Failed to publish lint event: $event", e)
            throw e
        }
    }
}
