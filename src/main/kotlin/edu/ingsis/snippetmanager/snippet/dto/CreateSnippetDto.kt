package edu.ingsis.snippetmanager.snippet.dto

data class CreateSnippetDto(
    val name: String,
    val description: String,
    val language: String,
    val version: String,
    val content: String,
    val extension: String,
)
