package edu.ingsis.snippetmanager.snippet.dto

data class CreateSnippetFileDto(
    val name: String,
    val description: String,
    val language: String,
    val extension: String,
)
