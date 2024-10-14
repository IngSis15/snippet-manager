package edu.ingsis.snippetmanager.external.printscript

import edu.ingsis.snippetmanager.external.printscript.dto.ValidateDTO
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.bodyToMono
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

    override fun validate(content: ValidateDTO): Mono<ValidateResultDTO> {
        return webClient.post()
            .uri("/v1/analyze")
            .bodyValue(content)
            .retrieve()
            .bodyToMono(ValidateResultDTO::class.java)
    }
}
