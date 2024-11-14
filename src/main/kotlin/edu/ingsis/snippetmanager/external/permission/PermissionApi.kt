package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono

interface PermissionApi {
    fun getAllSnippetPermissions(jwt: Jwt): Flux<PermissionResponseDTO>

    fun canRead(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<Boolean>

    fun canModify(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<Boolean>

    fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO>

    fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Mono<PermissionResponseDTO>

    fun getPermission(
        jwt: Jwt,
        snippetId: Long,
    ): Mono<PermissionResponseDTO>
}
