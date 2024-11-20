package edu.ingsis.snippetmanager.test.dto

data class TestResponse(
    val id: Long,
    val expectedOutput: List<String>,
    val userInput: List<String>,
    val testName: String,
)
