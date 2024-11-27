package edu.ingsis.snippetmanager.external.asset

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono

@Service
class AssetService(
    @Value("\${services.asset.url}") val baseUrl: String,
) : AssetApi {
    private val logger: Logger = LoggerFactory.getLogger(AssetService::class.java)
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
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { response ->
                val status = response.statusCode()
                logger.error("Error fetching asset: container={}, key={}, status={}", container, key, status)
                Mono.error(ResponseStatusException(status, "Error while fetching asset"))
            }
            .bodyToMono(String::class.java)
            .doOnNext { logger.info("Successfully fetched asset: container={}, key={}", container, key) }
            .onErrorResume { error ->
                logger.error("Error occurred while fetching asset: container={}, key={}, error={}", container, key, error.message)
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
            .doOnSuccess { logger.info("Successfully created asset: container={}, key={}", container, key) }
            .doOnError { error -> logger.error("Error creating asset: container={}, key={}, error={}", container, key, error.message) }
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
            .doOnSuccess { logger.info("Successfully deleted asset: container={}, key={}", container, key) }
            .doOnError { error -> logger.error("Error deleting asset: container={}, key={}, error={}", container, key, error.message) }
            .then()
    }
}
