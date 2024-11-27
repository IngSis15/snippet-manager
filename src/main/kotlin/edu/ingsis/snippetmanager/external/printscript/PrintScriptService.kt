package edu.ingsis.snippetmanager.external.printscript

import edu.ingsis.snippetmanager.external.printscript.dto.ExecuteRequestDto
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.snippet.dto.ExecuteResultDto
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Mono

@Service
class PrintScriptService(
    @Value("\${services.printscript.url}") val baseUrl: String,
) : PrintScriptApi {
    private val logger: Logger = LoggerFactory.getLogger(PrintScriptService::class.java)

    lateinit var webClient: WebClient

    @PostConstruct
    fun init() {
        webClient = WebClient.create(baseUrl)
    }

    override fun validate(content: String): Mono<ValidateResultDTO> {
        val token = getToken()

        if (token == null) {
            logger.warn("Token is null while validating content")
            return Mono.error(IllegalStateException("Missing authentication token"))
        }

        return webClient.post()
            .uri("/v1/validate")
            .headers { it.setBearerAuth(token) }
            .bodyValue(content)
            .retrieve()
            .bodyToMono(ValidateResultDTO::class.java)
            .doOnSuccess { logger.info("Validation successful for content length: {}", content.length) }
            .doOnError { ex ->
                if (ex is WebClientResponseException) {
                    logger.error("Validation failed with status: {}, body: {}", ex.statusCode, ex.responseBodyAsString)
                } else {
                    logger.error("Unexpected error during validation: {}", ex.message, ex)
                }
            }
    }

    override fun execute(
        snippetId: Long,
        input: List<String>,
    ): Mono<ExecuteResultDto> {
        val token = getToken()

        if (token == null) {
            logger.warn("Token is null while executing snippetId: {}", snippetId)
            return Mono.error(IllegalStateException("Missing authentication token"))
        }

        val dto = ExecuteRequestDto("snippets", snippetId.toString(), input)

        return webClient.post()
            .uri("/v1/execute")
            .headers { it.setBearerAuth(token) }
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(ExecuteResultDto::class.java)
            .doOnSuccess { logger.info("Execution successful for snippetId: {}", snippetId) }
            .doOnError { ex ->
                if (ex is WebClientResponseException) {
                    logger.error(
                        "Execution failed for snippetId: {}, status: {}, body: {}",
                        snippetId,
                        ex.statusCode,
                        ex.responseBodyAsString,
                    )
                } else {
                    logger.error("Unexpected error during execution for snippetId: {}", snippetId, ex)
                }
            }
    }

    private fun getToken(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication.principal is Jwt) {
            val jwt = authentication.principal as Jwt
            jwt.tokenValue
        } else {
            null
        }
    }
}
