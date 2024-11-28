package edu.ingsis.snippetmanager.external.asset

import edu.ingsis.snippetmanager.server.utils.LogTypes
import edu.ingsis.snippetmanager.server.utils.MdcLoggingUtils.logWithMdc
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.server.ResponseStatusException
import reactor.core.publisher.Mono
import reactor.core.publisher.Signal

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
        val correlationId = MDC.get("correlation-id")
        return webClient.get()
            .uri("/v1/asset/$container/$key")
            .retrieve()
            .onStatus({ it.is4xxClientError || it.is5xxServerError }) { response ->
                val status = response.statusCode()
                val errorSignal = Signal.error<String>(ResponseStatusException(status, "Error while fetching asset"))

                logWithMdc(
                    errorSignal,
                    LogTypes.ERROR,
                    logger,
                    "Error fetching asset: container=$container, key=$key, status=$status",
                    correlationId,
                )

                Mono.error(ResponseStatusException(status, "Error while fetching asset"))
            }
            .bodyToMono(String::class.java)
            .doOnNext {
                val successSignal = Signal.next(it)

                logWithMdc(
                    successSignal,
                    LogTypes.INFO,
                    logger,
                    "Successfully fetched asset: container=$container, key=$key",
                    correlationId,
                )
            }
            .onErrorResume { error ->
                val errorSignal = Signal.error<String>(error)

                logWithMdc(
                    errorSignal,
                    LogTypes.ERROR,
                    logger,
                    "Error occurred while fetching asset: container=$container, key=$key, error=${error.message}",
                    correlationId,
                )

                Mono.error(ResponseStatusException(HttpStatus.NOT_FOUND, "Asset not found"))
            }
    }

    override fun createAsset(
        container: String,
        key: String,
        content: String,
    ): Mono<Void> {
        val correlationId = MDC.get("correlation-id")
        return webClient.put()
            .uri("/v1/asset/$container/$key")
            .bodyValue(content)
            .retrieve()
            .toBodilessEntity()
            .doOnSuccess {
                val successSignal = Signal.next(it)

                logWithMdc(
                    successSignal,
                    LogTypes.INFO,
                    logger,
                    "Successfully created asset: container=$container, key=$key",
                    correlationId,
                )
            }
            .doOnError { error ->
                val errorSignal = Signal.error<Void>(error)

                logWithMdc(
                    errorSignal,
                    LogTypes.ERROR,
                    logger,
                    "Error creating asset: container=$container, key=$key, error=${error.message}",
                    correlationId,
                )
            }
            .then()
    }

    override fun deleteAsset(
        container: String,
        key: String,
    ): Mono<Void> {
        val correlationId = MDC.get("correlation-id")
        return webClient.delete()
            .uri("/v1/asset/$container/$key")
            .retrieve()
            .toBodilessEntity()
            .doOnSuccess {
                val successSignal = Signal.next(it)

                logWithMdc(
                    successSignal,
                    LogTypes.INFO,
                    logger,
                    "Successfully deleted asset: container=$container, key=$key",
                    correlationId,
                )
            }
            .doOnError { error ->
                val errorSignal = Signal.error<Void>(error)

                logWithMdc(
                    errorSignal,
                    LogTypes.ERROR,
                    logger,
                    "Error deleting asset: container=$container, key=$key, error=${error.message}",
                    correlationId,
                )
            }
            .then()
    }
}
