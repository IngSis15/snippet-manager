package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping

@RequestMapping("v1/snippet")
interface SnippetRoutesSpec {
    @GetMapping
    @Operation(summary = "Get all snippets")
    fun getAllSnippets(): List<Snippet>

    @GetMapping("/{id}")
    @Operation(
        summary = "Get snippet by id",
        parameters = [
            Parameter(name = "id", description = "snippet id", required = true),
        ],
    )
    fun getSnippet(
        @PathVariable id: Long,
    ): Snippet

    @PostMapping
    @Operation(summary = "Create new snippet")
    fun createSnippet(
        @RequestBody snippet: SnippetDto,
    ): Snippet

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete snippet from id",
        parameters = [
            Parameter(name = "id", description = "snippet id", required = true),
        ],
    )
    fun deleteSnippet(
        @PathVariable id: Long,
    )
}
