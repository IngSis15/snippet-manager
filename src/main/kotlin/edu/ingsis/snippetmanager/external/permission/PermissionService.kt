package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import org.springframework.beans.factory.annotation.Value
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.reactive.function.client.WebClient

@Component
class PermissionService(
    @Value("\${services.permission.url") private val baseUrl: String,
) : PermissionApi {
    lateinit var webClient: WebClient

    override fun getAllSnippetPermissions(jwt: Jwt): List<PermissionResponseDTO> {
        return webClient.get()
            .uri("/permissions/user")
            .headers { it.setBearerAuth(jwt.toString()) }
            .retrieve()
            .bodyToFlux(PermissionResponseDTO::class.java)
            .collectList()
            .block() ?: emptyList()
    }

    override fun checkPermission(
        jwt: Jwt,
        snippetId: Long,
    ): PermissionResponseDTO? {
        return webClient.get()
            .uri("/permissions/user/snippet/{snippetId}", snippetId)
            .headers { it.setBearerAuth(jwt.toString()) }
            .retrieve()
            .bodyToMono(PermissionResponseDTO::class.java)
            .block() ?: PermissionResponseDTO("permission_id", jwt.subject, snippetId, "permission_type")
    }

    override fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Boolean {
        val requestBody =
            mapOf(
                "snippetId" to snippetId,
                "permissionType" to permission,
            )

        return webClient.post()
            .uri("/permissions/user/snippet/{snippetId}/add", snippetId)
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .bodyValue(requestBody)
            .retrieve()
            .bodyToMono(Boolean::class.java)
            .block() ?: false
    }

    override fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Boolean {
        val requestBody =
            mapOf(
                "snippetId" to snippetId,
                "permissionType" to permission,
            )

        return webClient.delete()
            .uri("/permissions/user/snippet/{snippetId}/remove", snippetId)
            .headers { it.setBearerAuth(jwt.tokenValue) }
            .retrieve()
            .bodyToMono(Boolean::class.java)
            .block() ?: false
    }
}
