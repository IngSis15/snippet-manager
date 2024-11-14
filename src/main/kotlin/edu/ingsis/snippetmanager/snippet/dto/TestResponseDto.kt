package edu.ingsis.snippetmanager.snippet.dto

data class TestResponseDto(
    val passed: Boolean,
    val expectedOutput: List<String>,
    val actualOutput: List<String>,
)
