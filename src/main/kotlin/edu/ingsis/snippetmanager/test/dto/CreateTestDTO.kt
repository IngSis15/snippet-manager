package edu.ingsis.snippetmanager.test.dto

data class CreateTestDTO(
    val name: String,
    val snippetId: Long,
    val expectedOutput: List<String>,
    val userInput: List<String>,
)
