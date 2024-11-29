package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import org.springframework.security.oauth2.jwt.Jwt

interface PermissionApi {
    fun getAllSnippetPermissions(jwt: Jwt): List<PermissionResponseDTO>

    fun getAllOwnerSnippetPermissions(jwt: Jwt): List<PermissionResponseDTO>

    fun canRead(
        jwt: Jwt,
        snippetId: Long,
    ): Boolean

    fun canModify(
        jwt: Jwt,
        snippetId: Long,
    ): Boolean

    fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): PermissionResponseDTO?

    fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): PermissionResponseDTO?

    fun getPermission(
        jwt: Jwt,
        snippetId: Long,
    ): PermissionResponseDTO?
}
