package edu.ingsis.snippetmanager.external.asset

import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
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
            .bodyToMono(String::class.java)
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
