package edu.ingsis.snippetmanager.lint.consumer

import edu.ingsis.snippetmanager.lint.consumer.dto.LintResultDTO
import edu.ingsis.snippetmanager.redis.config.RedisStreamConsumer
import edu.ingsis.snippetmanager.snippet.SnippetService
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Profile
import org.springframework.data.redis.connection.stream.ObjectRecord
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.stream.StreamReceiver
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
@Profile("!test")
class LintStatusConsumer(
    @Value("\${stream.status.key}") streamKey: String,
    private val redisTemplate: RedisTemplate<String, String>,
    private val snippetService: SnippetService,
) : RedisStreamConsumer<String>(streamKey, "status-group", redisTemplate) {
    private val logger = System.getLogger(LintStatusConsumer::class.simpleName)

    override fun options(): StreamReceiver.StreamReceiverOptions<String, ObjectRecord<String, String>> {
        return StreamReceiver.StreamReceiverOptions.builder()
            .pollTimeout(java.time.Duration.ofMillis(10000)) // Set poll rate
            .targetType(String::class.java) // Set type to de-serialize record
            .build()
    }

    override fun onMessage(record: ObjectRecord<String, String>) {
        val lintingResultDto = Json.decodeFromString<LintResultDTO>(record.value)
        logger.log(
            System.Logger.Level.INFO,
            "Updating linting compliance for snippet ${lintingResultDto.snippetId} to ${lintingResultDto.ok}",
        )

        Mono.fromRunnable<Unit> {
            snippetService.updateLintingCompliance(lintingResultDto.snippetId, lintingResultDto.ok)
        }
            .doOnError { e ->
                logger.log(System.Logger.Level.ERROR, "Error updating linting compliance: ${e.message}", e)
            }
            .subscribe()
    }
}
