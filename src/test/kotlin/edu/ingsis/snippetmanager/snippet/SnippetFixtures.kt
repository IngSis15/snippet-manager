package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto

object SnippetFixtures {
    fun all(): List<CreateSnippetDto> =
        listOf(
            CreateSnippetDto(
                title = "Declaration",
                description = "This snippet declares a variable x",
                version = "1.1",
                content = "let x: number = 10;",
            ),
            CreateSnippetDto(
                title = "Hello World",
                description = "Prints \"Hello, World!\"",
                version = "1.1",
                content = "println(\"Hello, World!\")",
            ),
        )
}
