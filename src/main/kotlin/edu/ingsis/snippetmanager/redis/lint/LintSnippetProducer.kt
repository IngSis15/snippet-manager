package edu.ingsis.snippetmanager.redis.lint

import edu.ingsis.snippetmanager.redis.config.RedisStreamProducer
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.ReactiveRedisTemplate
import org.springframework.stereotype.Component
import java.lang.System.getLogger

@Component
class LintSnippetProducer
    @Autowired
    constructor(
        @Value("\${stream.key}") streamKey: String,
        redis: ReactiveRedisTemplate<String, String>,
    ) : RedisStreamProducer(streamKey, redis) {
        val logger: System.Logger = getLogger(LintSnippetProducer::class.simpleName)

        suspend fun lintSnippet(lintSnippetDto: LintSnippetDto) {
            logger.log(System.Logger.Level.INFO, "Linting snippet with id: ${lintSnippetDto.snippetId}")
            emit(lintSnippetDto).awaitSingle()
        }
    }
