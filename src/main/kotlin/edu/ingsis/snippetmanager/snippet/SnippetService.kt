package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateDTO
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException

@Component
class SnippetService
    @Autowired
    constructor(
        private val repository: SnippetRepository,
        private val printScriptService: PrintScriptApi,
    ) {
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

            val validation =
                printScriptService.validate(ValidateDTO(snippet.content, "1.0")).block()
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating snippet")

            if (!validation.ok) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid snippet")
            }

            return repository.save(snippet)
        }

        @Transactional
        fun deleteSnippet(id: Long) {
            return repository.deleteById(id)
        }
    }
