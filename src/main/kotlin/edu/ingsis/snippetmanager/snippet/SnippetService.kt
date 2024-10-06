package edu.ingsis.snippetmanager.snippet

import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class SnippetService
    @Autowired
    constructor(private val repository: SnippetRepository) {
        fun getAllSnippets(): List<Snippet> {
            return repository.findAll().toList()
        }

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
            return repository.save(snippet)
        }

        @Transactional
        fun deleteSnippet(id: Long) {
            return repository.deleteById(id)
        }
    }
