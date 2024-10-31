package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.Snippet

object TestFixtures {
    fun all(snippet: Snippet): List<Test> = listOf(
        Test(
            snippet = snippet,
            expectedOutput = "Expected Output 1",
            userInput = "User Input 1",
            environmentVariables = mapOf("ENV_VAR1" to "value1")
        ),
        Test(
            snippet = snippet,
            expectedOutput = "Expected Output 2",
            userInput = "User Input 2",
            environmentVariables = mapOf("ENV_VAR2" to "value2")
        )
    )
}
