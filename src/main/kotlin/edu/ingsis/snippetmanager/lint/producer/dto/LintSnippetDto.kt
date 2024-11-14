package edu.ingsis.snippetmanager.lint.producer.dto

import kotlinx.serialization.Serializable

@Serializable
class LintSnippetDto(
    val snippetId: Long,
    val configId: String,
)
