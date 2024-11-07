package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import org.springframework.security.oauth2.jwt.Jwt

interface PermissionApi {
    fun getAllSnippetPermissions(jwt: Jwt): List<PermissionResponseDTO>

    fun checkPermission(jwt: Jwt, snippetId: Long): PermissionResponseDTO?

    fun addPermission(jwt: Jwt, snippetId: Long, permission: String): Boolean

    fun removePermission(jwt: Jwt, snippetId: Long, permission: String): Boolean
}