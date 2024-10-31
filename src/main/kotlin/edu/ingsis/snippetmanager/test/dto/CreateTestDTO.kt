package edu.ingsis.snippetmanager.test.dto

data class CreateTestDTO(
    val snippetId: Long,
    val expectedOutput: String,
    val userInput: String,
    val environmentVariables: Map<String, String> = emptyMap()
)

