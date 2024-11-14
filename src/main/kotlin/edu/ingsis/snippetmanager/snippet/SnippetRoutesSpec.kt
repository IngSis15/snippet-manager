package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import edu.ingsis.snippetmanager.snippet.dto.TestResponseDto
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import org.springframework.data.domain.Page
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.multipart.MultipartFile

@RequestMapping("v1/snippet")
interface SnippetRoutesSpec {
    @GetMapping
    @Operation(summary = "Get all snippets")
    fun getAllSnippets(
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): ResponseEntity<Page<SnippetDto>>

    @GetMapping("/{id}")
    @Operation(
        summary = "Get snippet by id",
        parameters = [
            Parameter(name = "id", description = "snippet id", required = true),
        ],
    )
    fun getSnippet(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SnippetDto>

    @PostMapping
    @Operation(summary = "Create new snippet")
    fun createSnippet(
        @RequestBody snippet: CreateSnippetDto,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SnippetDto>

    @PostMapping("/{id}")
    @Operation(summary = "Edit snippet")
    fun editSnippet(
        @RequestBody snippet: CreateSnippetDto,
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SnippetDto>

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete snippet from id",
        parameters = [
            Parameter(name = "id", description = "snippet id", required = true),
        ],
    )
    fun deleteSnippet(
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    )

    @PostMapping("/upload", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Upload snippet from file")
    fun uploadSnippet(
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestParam language: String,
        @RequestParam version: String,
        @RequestParam extension: String,
        @RequestParam file: MultipartFile,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SnippetDto>

    @PostMapping("/upload/{id}", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(summary = "Edit snippet from upload")
    fun editUploadSnippet(
        @RequestParam name: String,
        @RequestParam description: String,
        @RequestParam language: String,
        @RequestParam version: String,
        @RequestParam extension: String,
        @RequestParam file: MultipartFile,
        @PathVariable id: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<SnippetDto>

    @GetMapping("/user")
    @Operation(summary = "Get all snippets from user")
    fun getSnippetsByUser(
        @AuthenticationPrincipal jwt: Jwt,
        @RequestParam page: Int,
        @RequestParam size: Int,
    ): ResponseEntity<Page<SnippetDto>>

    @PutMapping("/{id}")
    @Operation(summary = "Update snippet from string")
    fun updateFromString(
        @RequestBody snippet: String,
        @AuthenticationPrincipal jwt: Jwt,
        @PathVariable id: Long,
    ): ResponseEntity<SnippetDto>

    @GetMapping("/test/{testId}")
    @Operation(summary = "Run test for snippet")
    fun runTest(
        @PathVariable testId: Long,
        @AuthenticationPrincipal jwt: Jwt,
    ): ResponseEntity<TestResponseDto>
}
