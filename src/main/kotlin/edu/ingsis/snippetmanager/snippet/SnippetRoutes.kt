package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import edu.ingsis.snippetmanager.snippet.dto.StatusDto
import edu.ingsis.snippetmanager.snippet.dto.TestResponseDto
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SnippetRoutes
    @Autowired
    constructor(private val service: SnippetService, private val testSnippetService: TestSnippetService) :
    SnippetRoutesSpec {
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

        override fun getSnippetsByUser(
            jwt: Jwt,
            page: Int,
            size: Int,
        ): ResponseEntity<Page<SnippetDto>> {
            val pageable: Pageable = PageRequest.of(page, size)
            return ResponseEntity.ok(service.getSnippetsByUser(jwt, pageable))
        }

        override fun updateFromString(
            snippet: String,
            jwt: Jwt,
            id: Long,
        ): ResponseEntity<SnippetDto> {
            return ResponseEntity.ok(service.updateFromString(snippet, jwt, id))
        }

        override fun runTest(
            testId: Long,
            jwt: Jwt,
        ): ResponseEntity<TestResponseDto> {
            return ResponseEntity.ok(testSnippetService.runTest(testId, jwt))
        }

        override fun updateLintStatus(statusDto: StatusDto): ResponseEntity<Unit> {
            return ResponseEntity.ok(service.updateLintingCompliance(statusDto))
        }

        override fun formatSnippet(id: Long): ResponseEntity<String> {
            return ResponseEntity.ok(service.formatSnippet(id))
        }
    }
