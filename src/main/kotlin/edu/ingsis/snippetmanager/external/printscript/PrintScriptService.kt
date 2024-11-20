package edu.ingsis.snippetmanager.external.printscript

import edu.ingsis.snippetmanager.external.printscript.dto.ExecuteRequestDto
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.snippet.dto.ExecuteResultDto
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono

@Component
class PrintScriptService(
    @Value("\${services.printscript.url}") val baseUrl: String,
) : PrintScriptApi {
    lateinit var webClient: WebClient

    @PostConstruct
    fun init() {
        webClient = WebClient.create(baseUrl)
    }

    override fun validate(content: String): Mono<ValidateResultDTO> {
        val token = getToken()

        return webClient.post()
            .uri("/v1/validate")
            .headers { it.setBearerAuth(token.toString()) }
            .bodyValue(content)
            .retrieve()
            .bodyToMono(ValidateResultDTO::class.java)
    }

    override fun execute(
        snippetId: Long,
        input: List<String>,
    ): Mono<ExecuteResultDto> {
        val token = getToken()
        val dto = ExecuteRequestDto("snippets", snippetId.toString(), input)

        return webClient.post()
            .uri("/v1/execute")
            .headers { it.setBearerAuth(token.toString()) }
            .bodyValue(dto)
            .retrieve()
            .bodyToMono(ExecuteResultDto::class.java)
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
