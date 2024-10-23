package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetFileDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class SnippetRoutes
    @Autowired
    constructor(private val service: SnippetService) : SnippetRoutesSpec {
        override fun getAllSnippets(): ResponseEntity<List<SnippetDto>> {
            return ResponseEntity.ok(service.getAllSnippets())
        }

        override fun getSnippet(
            @PathVariable id: Long,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity.ok(service.getSnippet(id))
        }

        override fun createSnippet(
            @RequestBody snippet: CreateSnippetDto,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity.ok(service.createSnippet(snippet))
        }

        override fun deleteSnippet(
            @PathVariable id: Long,
        ) {
            service.deleteSnippet(id)
        }

        override fun uploadSnippet(
            snippet: CreateSnippetFileDto,
            file: MultipartFile,
        ): ResponseEntity<SnippetDto> {
            TODO("Not yet implemented")
        }
    }
