package edu.ingsis.snippetmanager.external.printscript.dto

data class ExecuteRequestDto(
    val container: String,
    val key: String,
    val input: List<String>,
)
