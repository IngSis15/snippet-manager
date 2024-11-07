package edu.ingsis.snippetmanager.external

import edu.ingsis.snippetmanager.external.permission.PermissionApi
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import org.springframework.security.oauth2.jwt.Jwt

class PermissionMockApi : PermissionApi {
    override fun getAllSnippetPermissions(jwt: Jwt): List<PermissionResponseDTO> {
        return listOf(
            PermissionResponseDTO("1", jwt.subject, 1L, "owner"),
            PermissionResponseDTO("2", jwt.subject, 2L, "viewer"),
        )
    }

    override fun checkPermission(
        jwt: Jwt,
        snippetId: Long,
    ): PermissionResponseDTO? {
        return PermissionResponseDTO("1", jwt.subject, snippetId, "owner")
    }

    override fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Boolean {
        return true
    }

    override fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): Boolean {
        return true
    }
}
