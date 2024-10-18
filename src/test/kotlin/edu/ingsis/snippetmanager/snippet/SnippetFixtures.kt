package edu.ingsis.snippetmanager.snippet

object SnippetFixtures {
    fun all(): List<Snippet> =
        listOf(
            Snippet(
                title = "Declaration",
                description = "This snippet declares a variable x",
                version = "1.1",
                content = "let x: number = 5;",
            ),
            Snippet(
                title = "Hello World",
                description = "Prints \"Hello, World!\"",
                version = "1.1",
                content = "println(\"Hello, World!\");",
            ),
        )
}
