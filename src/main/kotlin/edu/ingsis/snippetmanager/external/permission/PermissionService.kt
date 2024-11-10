package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import jakarta.annotation.PostConstruct
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
            .onErrorReturn(WebClientResponseException.NotFound::class.java, false)
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
            .onErrorReturn(WebClientResponseException.NotFound::class.java, false)
    }

    override fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO> {
        val requestBody =
            mapOf(
                "snippetId" to snippetId,
                "permissionType" to permission,
            )

        return webClient.post()
            .uri("/permissions/assign", snippetId)
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
    }

    override fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO> {
        val requestBody =
            mapOf(
                "snippetId" to snippetId,
                "permissionType" to permission,
            )

        return webClient.delete()
            .uri { uriBuilder ->
                uriBuilder
                    .path("/permissions/user/snippet/{snippetId}")
                    .queryParam("permissionType", permission)
                    .build(snippetId)
            }
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
    }
}
