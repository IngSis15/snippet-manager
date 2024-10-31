package edu.ingsis.snippetmanager.test.dto

data class UpdateTestDTO(
    val expectedOutput: String,
    val userInput: String,
    val environmentVariables: Map<String, String> = emptyMap()
)
