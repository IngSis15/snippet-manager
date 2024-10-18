package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SnippetRoutes
    @Autowired
    constructor(private val service: SnippetService) : SnippetRoutesSpec {
        override fun getAllSnippets(): List<Snippet> {
            return service.getAllSnippets()
        }

        override fun getSnippet(
            @PathVariable id: Long,
        ): Snippet {
            return service.getSnippet(id)
        }

        override fun createSnippet(
            @RequestBody snippet: SnippetDto,
        ): Snippet {
            return service.createSnippet(snippet)
        }

        override fun deleteSnippet(
            @PathVariable id: Long,
        ) {
            service.deleteSnippet(id)
        }
    }
