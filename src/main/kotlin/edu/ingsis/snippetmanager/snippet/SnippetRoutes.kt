package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetFileDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
class SnippetRoutes
    @Autowired
    constructor(private val service: SnippetService) : SnippetRoutesSpec {
        override fun getAllSnippets(jwt: Jwt): ResponseEntity<List<SnippetDto>> {
            return ResponseEntity.ok(service.getSnippetsByUser(jwt))
        }

        override fun getSnippet(
            @PathVariable id: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity.ok(service.getSnippet(id, jwt))
        }

        override fun createSnippet(
            @RequestBody snippet: CreateSnippetDto,
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity.status(HttpStatus.CREATED).body(service.createSnippet(snippet, jwt))
        }

        override fun editSnippet(
            snippet: CreateSnippetDto,
            id: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity.ok(service.editSnippet(snippet, id, jwt))
        }

        override fun deleteSnippet(
            @PathVariable id: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ) {
            service.deleteSnippet(id, jwt)
        }

        override fun uploadSnippet(
            @RequestParam name: String,
            @RequestParam description: String,
            @RequestParam language: String,
            @RequestParam version: String,
            @RequestParam extension: String,
            @RequestParam file: MultipartFile,
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                    service.createFromFile(
                        CreateSnippetFileDto(
                            name,
                            description,
                            language,
                            version,
                            extension,
                        ),
                        file,
                        jwt,
                    ),
                )
        }

        override fun editUploadSnippet(
            @RequestParam name: String,
            @RequestParam description: String,
            @RequestParam language: String,
            @RequestParam version: String,
            @RequestParam extension: String,
            @RequestParam file: MultipartFile,
            @PathVariable id: Long,
            @AuthenticationPrincipal jwt: Jwt,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity.ok(
                service.editFromFile(
                    CreateSnippetFileDto(
                        name,
                        description,
                        language,
                        version,
                        extension,
                    ),
                    file,
                    id,
                    jwt,
                ),
            )
        }

        override fun getSnippetsByUser(jwt: Jwt): ResponseEntity<List<SnippetDto>> {
            return ResponseEntity.ok(service.getSnippetsByUser(jwt))
        }
    }
