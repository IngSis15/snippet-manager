package edu.ingsis.snippetmanager.snippet.dto

data class SnippetDto(
    val id: Long?,
    val title: String,
    val description: String,
    val version: String,
    val content: String,
)
