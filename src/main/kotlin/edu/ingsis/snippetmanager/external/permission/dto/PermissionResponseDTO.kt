package edu.ingsis.snippetmanager.external.permission.dto

data class PermissionResponseDTO(
    val id: String,
    val userId: String,
    val snippetId: Long,
    val permissionType: String,
)