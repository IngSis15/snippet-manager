package edu.ingsis.snippetmanager.test.dto

data class CreateTestDTO(
    val snippetId: Long,
    val expectedOutput: List<String>,
    val userInput: List<String>,
    val name: String,
)
