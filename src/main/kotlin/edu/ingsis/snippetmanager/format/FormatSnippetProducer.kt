package edu.ingsis.snippetmanager.format

import org.austral.ingsis.redis.RedisStreamProducer
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.lang.System.getLogger

interface FormatSnippetProducer {
    fun publishEvent(event: String)
}

@Component
class RedisFormatSnippetProducer
    @Autowired
    constructor(
        @Value("\${stream.format.key}") streamKey: String,
        redis: RedisTemplate<String, String>,
    ) : FormatSnippetProducer, RedisStreamProducer(streamKey, redis) {
        val logger: System.Logger = getLogger(FormatSnippetProducer::class.simpleName)

        override fun publishEvent(event: String) {
            logger.log(System.Logger.Level.INFO, "Formatting snippet: $event")
            emit(event)
        }
    }
