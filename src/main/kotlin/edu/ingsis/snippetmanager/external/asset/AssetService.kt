package edu.ingsis.snippetmanager.external.asset

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Component
class AssetService(
    @Value("\${services.asset.url}") val baseUrl: String,
) : AssetApi {
    lateinit var webClient: WebClient

    @PostConstruct
    fun init() {
        webClient = WebClient.create(baseUrl)
    }

    override fun getAsset(
        container: String,
        key: String,
    ): Mono<String> {
        return webClient.get()
            .uri("/v1/asset/$container/$key")
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) {
                Mono.error(ResponseStatusException(it.statusCode(), "Error while fetching asset"))
            }
            .bodyToMono(String::class.java)
            .onErrorResume {
                Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"))
            }
    }

    override fun createAsset(
        container: String,
        key: String,
        content: String,
    ): Mono<Void> {
        return webClient.put()
            .uri("/v1/asset/$container/$key")
            .bodyValue(content)
            .retrieve()
            .toBodilessEntity()
            .then()
    }

    override fun deleteAsset(
        container: String,
        key: String,
    ): Mono<Void> {
        return webClient.delete()
            .uri("/v1/asset/$container/$key")
            .retrieve()
            .toBodilessEntity()
            .then()
    }
}
