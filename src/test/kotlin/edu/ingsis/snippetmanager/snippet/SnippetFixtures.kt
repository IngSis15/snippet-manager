package edu.ingsis.snippetmanager.snippet

object SnippetFixtures {
    fun all(): List<Snippet> =
        listOf(
            Snippet(
                "Declaration",
                "This snippet declares a variable x",
                "let x: number = 5;",
                1,
            ),
            Snippet(
                "Hello World",
                "Prints \"Hello, World!\"",
                "println(\"Hello, World!\");",
                2,
            ),
        )
}
