package edu.ingsis.snippetmanager.snippet.dto

data class SnippetDto(
    val id: Long?,
    val name: String,
    val description: String,
    val language: String,
    val compliance: String,
    val extension: String,
    val content: String,
    val permission: String,
    val author: String,
)
