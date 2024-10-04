package edu.ingsis.snippetmanager.snippet

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/snippet")
class SnippetController
    @Autowired
    constructor(private val service: SnippetService) {
        @PostMapping
        fun createSnippet(
            @RequestBody snippet: SnippetDto,
        ): Snippet {
            return service.createSnippet(snippet.title, snippet.description, snippet.content)
        }

        @GetMapping("/{id}")
        fun getSnippetById(
            @PathVariable id: Long,
        ): Snippet? {
            return service.getSnippet(id)
        }
    }
