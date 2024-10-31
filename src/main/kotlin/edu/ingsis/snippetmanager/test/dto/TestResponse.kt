package edu.ingsis.snippetmanager.test.dto

data class TestResponse(
    val id: Long,
    val expectedOutput: String,
    val userInput: String,
    val environmentVariables: Map<String, String>
)
