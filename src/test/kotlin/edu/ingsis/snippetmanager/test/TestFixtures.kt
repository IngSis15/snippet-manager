package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.Snippet

object TestFixtures {
    fun all(snippet: Snippet): List<Test> =
        listOf(
            Test(
                snippet = snippet,
                expectedOutput = listOf("Expected Output Line 1", "Expected Output Line 2"),
                userInput = listOf("User Input Line 1", "User Input Line 2")
            ),
            Test(
                snippet = snippet,
                expectedOutput = listOf("Expected Output Line 3", "Expected Output Line 4"),
                userInput = listOf("User Input Line 3", "User Input Line 4")
            )
        )
}
