package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.WebClientResponseException
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

@Component
class PermissionService(
    @Value("\${services.permission.url}") val baseUrl: String,
) : PermissionApi {
    private val logger: Logger = LoggerFactory.getLogger(PermissionService::class.java)
    lateinit var webClient: WebClient

    @PostConstruct
    fun init() {
        webClient = WebClient.create(baseUrl)
    }

    override fun getAllSnippetPermissions(jwt: Jwt): Flux<PermissionResponseDTO> {
        return webClient.get()
            .uri("/permissions/user")
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToFlux(PermissionResponseDTO::class.java)
            .doOnNext { logger.debug("Fetched permission: {}", it) }
            .doOnComplete { logger.info("Completed fetching all snippet permissions") }
            .doOnError { logger.error("Error fetching all snippet permissions: {}", it.message) }
    }

    override fun getAllOwnerSnippetPermissions(jwt: Jwt): Flux<PermissionResponseDTO> {
        return webClient.get()
            .uri { uriBuilder ->
                uriBuilder.path("/permissions/permissionType")
                    .queryParam("permissionType", "OWNER")
                    .build()
            }
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToFlux(PermissionResponseDTO::class.java)
            .doOnNext { logger.debug("Fetched owner permission: {}", it) }
            .doOnComplete { logger.info("Completed fetching owner snippet permissions") }
            .doOnError { logger.error("Error fetching owner snippet permissions: {}", it.message) }
    }

    override fun canRead(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<Boolean> {
        return webClient.get()
            .uri("/permissions/user/snippet/$snippetId")
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
            .map { true }
            .doOnSuccess { logger.info("Read permission granted for snippetId={}", snippetId) }
            .onErrorResume(WebClientResponseException.NotFound::class.java) {
                logger.warn("Read permission denied for snippetId={}: Not Found", snippetId)
                Mono.just(false)
            }
            .doOnError { logger.error("Error checking read permission for snippetId={}: {}", snippetId, it.message) }
    }

    override fun canModify(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<Boolean> {
        return webClient.get()
            .uri("/permissions/user/snippet/$snippetId")
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
            .map { it.permissionType == "OWNER" }
            .doOnSuccess { logger.info("Modify permission result for snippetId={}: {}", snippetId, it) }
            .onErrorResume(WebClientResponseException.NotFound::class.java) {
                logger.warn("Modify permission denied for snippetId={}: Not Found", snippetId)
                Mono.just(false)
            }
            .doOnError { logger.error("Error checking modify permission for snippetId={}: {}", snippetId, it.message) }
    }

    override fun getPermission(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<PermissionResponseDTO> {
        return webClient.get()
            .uri("/permissions/user/snippet/$snippetId")
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
            .doOnSuccess { logger.info("Fetched permission for snippetId={}: {}", snippetId, it) }
            .doOnError { logger.error("Error fetching permission for snippetId={}: {}", snippetId, it.message) }
    }

    override fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO> {
        val requestBody = mapOf("snippetId" to snippetId, "permissionType" to permission)

        return webClient.post()
            .uri("/permissions/assign", snippetId)
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
            .doOnSuccess { logger.info("Added permission for snippetId={}: {}", snippetId, it) }
            .doOnError { logger.error("Error adding permission for snippetId={}: {}", snippetId, it.message) }
    }

    override fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO> {
        return webClient.delete()
            .uri { uriBuilder ->
                uriBuilder.path("/permissions/user/snippet/{snippetId}")
                    .queryParam("permissionType", permission)
                    .build(snippetId)
            }
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
            .doOnSuccess { logger.info("Removed permission for snippetId={}: {}", snippetId, it) }
            .doOnError { logger.error("Error removing permission for snippetId={}: {}", snippetId, it.message) }
    }
}
