package edu.ingsis.snippetmanager.snippet

object SnippetFixtures {
    private val snippets =
        listOf(
            Snippet(
                id = 1L,
                name = "Declaration",
                description = "This snippet declares a variable x",
                language = "printscript",
                version = "1.1",
                extension = "ps",
            ),
            Snippet(
                id = 2L,
                name = "Hello World",
                description = "Prints \"Hello, World!\"",
                language = "printscript",
                version = "1.1",
                extension = "ps",
            ),
        )

    fun all(): List<Snippet> = snippets

    fun getContentFromName(name: String): String {
        return when (name) {
            "Declaration" -> "let x: number = 10;"
            "Hello World" -> "print(\"Hello, World!\")"
            else -> throw IllegalArgumentException("Invalid snippet name")
        }
    }
}
