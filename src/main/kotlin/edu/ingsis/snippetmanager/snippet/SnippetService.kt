package edu.ingsis.snippetmanager.snippet

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class SnippetService
    @Autowired
    constructor(private val repository: SnippetRepository) {
        fun getSnippet(id: Long): Snippet {
            val snippet =
                repository.findSnippetById(id)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")

            return snippet
        }

        fun createSnippet(
            title: String,
            description: String,
            content: String,
        ): Snippet {
            val snippet = Snippet(title, description, content)
            repository.save(snippet)
            return snippet
        }
    }
