package edu.ingsis.snippetmanager.lint.dto

import kotlinx.serialization.Serializable

@Serializable
class LintSnippetDto(
    val snippetId: Long,
    val configId: String,
    val correlationId: String,
)
