package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient
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
            .uri("/all/user")
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToFlux(PermissionResponseDTO::class.java)
    }

    override fun checkPermission(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<PermissionResponseDTO> {
        return webClient.get()
            .uri("/permissions/user/snippet/$snippetId")
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
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
            .uri("/permissions/user/snippet/{snippetId}/remove", snippetId)
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
    }
}
