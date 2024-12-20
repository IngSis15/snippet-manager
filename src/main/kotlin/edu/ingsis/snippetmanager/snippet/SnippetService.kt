package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.format.FormatService
import edu.ingsis.snippetmanager.lint.LintService
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import edu.ingsis.snippetmanager.snippet.dto.StatusDto
import jakarta.transaction.Transactional
import org.slf4j.LoggerFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException

@Service
class SnippetService(
    private val repository: SnippetRepository,
    private val printScriptService: PrintScriptApi,
    private val assetService: AssetApi,
    private val permissionService: PermissionService,
    private val lintService: LintService,
    private val formatService: FormatService,
) {
    private val logger = LoggerFactory.getLogger(SnippetService::class.java)

    fun getSnippet(
        id: Long,
        jwt: Jwt,
    ): SnippetDto {
        val canRead = permissionService.canRead(jwt, id)
        if (!canRead) {
            logger.warn("Permission denied for user: ${jwt.subject} to read snippet: $id")
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
        }
        val permission =
            permissionService.getPermission(jwt, id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
        val snippet =
            repository.findSnippetById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")
        val content = fetchSnippetContent(id)
        logger.info("Successfully fetched snippet with ID: $id")
        return translate(snippet, content, permission, snippet.compliance)
    }

    fun createSnippet(
        snippetDto: CreateSnippetDto,
        jwt: Jwt,
    ): SnippetDto {
        validateSnippetContent(snippetDto.content)
        val snippet = translate(snippetDto)
        val savedSnippet = repository.save(snippet)
        logger.info("Snippet created with ID: ${savedSnippet.id}")

        assetService.createAsset("snippets", savedSnippet.id.toString(), snippetDto.content)
        lintService.lintSnippet(savedSnippet.id!!, jwt.subject)
        formatService.formatSnippet(savedSnippet.id!!, jwt.subject)
        val permission = permissionService.addPermission(jwt, savedSnippet.id!!, "OWNER")!!
        logger.info("Snippet created and permissions assigned for ID: ${savedSnippet.id}")
        return translate(savedSnippet, snippetDto.content, permission, savedSnippet.compliance)
    }

    fun editSnippet(
        snippetDto: CreateSnippetDto,
        id: Long,
        jwt: Jwt,
    ): SnippetDto {
        validateSnippetContent(snippetDto.content)
        val canModify = permissionService.canModify(jwt, id)
        if (!canModify) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
        }

        val permission =
            permissionService.getPermission(jwt, id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
        assetService.createAsset("snippets", id.toString(), snippetDto.content)

        val savedSnippet =
            repository.save(
                Snippet(
                    id,
                    snippetDto.name,
                    snippetDto.description,
                    snippetDto.language,
                    Compliance.PENDING,
                    snippetDto.extension,
                ),
            )

        lintService.lintSnippet(savedSnippet.id!!, jwt.subject)
        formatService.formatSnippet(savedSnippet.id!!, jwt.subject)

        return translate(savedSnippet, snippetDto.content, permission, savedSnippet.compliance)
    }

    @Transactional
    fun deleteSnippet(
        id: Long,
        jwt: Jwt,
    ) {
        val canModify = permissionService.canModify(jwt, id)
        if (!canModify) {
            logger.warn("Permission denied for user: ${jwt.subject} to delete snippet: $id")
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
        }
        repository.deleteById(id)
        permissionService.removePermission(jwt, id, "OWNER")
        assetService.deleteAsset("snippets", id.toString())
        logger.info("Snippet with ID: $id successfully deleted")
    }

    private fun fetchSnippetContent(id: Long): String {
        return assetService.getAsset("snippets", id.toString())
            ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet content not found")
    }

    private fun validateSnippetContent(content: String) {
        val validation =
            printScriptService.validate(content)
                ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating snippet")
        if (!validation.ok) {
            logger.warn("Snippet content validation failed")
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid snippet content")
        }
        logger.info("Snippet content successfully validated")
    }

    fun getSnippetsByUser(
        jwt: Jwt,
        pageable: Pageable,
    ): Page<SnippetDto> {
        val permissions = permissionService.getAllSnippetPermissions(jwt)
        val snippets =
            permissions.mapNotNull { permission ->
                val snippet = repository.findSnippetById(permission.snippetId)
                snippet?.let {
                    val content = fetchSnippetContent(it.id!!)
                    translate(it, content, permission, snippet.compliance)
                }
            }
        val start = pageable.offset.toInt()
        val end = (start + pageable.pageSize).coerceAtMost(snippets.size)
        val pagedSnippets = snippets.subList(start, end)
        return PageImpl(pagedSnippets, pageable, snippets.size.toLong())
    }

    fun updateFromString(
        snippet: String,
        jwt: Jwt,
        id: Long,
    ): SnippetDto {
        validateSnippetContent(snippet)
        val canModify = permissionService.canModify(jwt, id)
        if (!canModify) {
            throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
        }

        val permissionResponse =
            permissionService.getPermission(jwt, id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")

        val originalSnippet =
            repository.findSnippetById(id)
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")

        assetService.createAsset("snippets", id.toString(), snippet)

        val savedSnippet =
            repository.save(
                Snippet(
                    id,
                    originalSnippet.name,
                    originalSnippet.description,
                    originalSnippet.language,
                    Compliance.PENDING,
                    originalSnippet.extension,
                ),
            )

        lintService.lintSnippet(savedSnippet.id!!, jwt.subject)
        formatService.formatSnippet(savedSnippet.id!!, jwt.subject)

        return translate(savedSnippet, snippet, permissionResponse, Compliance.PENDING)
    }

    fun updateLintingCompliance(statusDto: StatusDto) {
        val snippet = repository.findSnippetById(statusDto.snippetId)
        snippet?.let {
            repository.save(
                Snippet(
                    id = snippet.id,
                    name = snippet.name,
                    description = snippet.description,
                    language = snippet.language,
                    compliance = statusDto.compliance,
                    extension = snippet.extension,
                ),
            )
        }
    }

    fun formatSnippet(snippetId: Long): String {
        val formatted =
            assetService.getAsset("formatted", snippetId.toString())
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")

        return formatted
    }

    private fun translate(snippetDto: CreateSnippetDto) =
        Snippet(
            name = snippetDto.name,
            description = snippetDto.description,
            language = snippetDto.language,
            compliance = Compliance.PENDING,
            extension = snippetDto.extension,
        )

    private fun translate(
        snippet: Snippet,
        content: String,
        permissionResponse: PermissionResponseDTO,
        compliance: Compliance,
    ) = SnippetDto(
        id = snippet.id,
        name = snippet.name,
        description = snippet.description,
        language = snippet.language,
        content = content,
        extension = snippet.extension,
        permission = permissionResponse.permissionType,
        author = permissionResponse.username,
        compliance = compliance.toString(),
    )
}
