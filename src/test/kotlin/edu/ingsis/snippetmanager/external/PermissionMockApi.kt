package edu.ingsis.snippetmanager.external

import edu.ingsis.snippetmanager.external.permission.PermissionApi
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

class PermissionMockApi : PermissionApi {
    override fun getAllSnippetPermissions(jwt: Jwt): Flux<PermissionResponseDTO> {
        val permissions = listOf(
            PermissionResponseDTO("1", jwt.subject, 1L, "owner"),
            PermissionResponseDTO("2", jwt.subject, 2L, "viewer"),
        )
        return Flux.fromIterable(permissions)
    }

    override fun canRead(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<Boolean> {
        return Mono.just(true)
    }

    override fun canModify(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<Boolean> {
        return Mono.just(true)
    }

    override fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO> {
        return Mono.just(PermissionResponseDTO("1", jwt.subject, snippetId, permission))
    }

    override fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO> {
        return Mono.just(PermissionResponseDTO("1", jwt.subject, snippetId, permission))
    }
}