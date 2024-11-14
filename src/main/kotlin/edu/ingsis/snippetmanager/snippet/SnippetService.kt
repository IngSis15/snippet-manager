package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.format.FormatService
import edu.ingsis.snippetmanager.lint.producer.LintService
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetFileDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component
import org.springframework.web.multipart.MultipartFile
import org.springframework.web.server.ResponseStatusException

@Component
class SnippetService
    @Autowired
    constructor(
        private val repository: SnippetRepository,
        private val printScriptService: PrintScriptApi,
        private val assetService: AssetApi,
        private val permissionService: PermissionService,
        private val lintService: LintService,
        private val formatService: FormatService,
    ) {
        fun getSnippet(
            id: Long,
            jwt: Jwt,
        ): SnippetDto {
            val canRead = permissionService.canRead(jwt, id).block() ?: false
            if (!canRead) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
            }
            val permission =
                permissionService.getPermission(jwt, id).block()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
            val snippet =
                repository.findSnippetById(id)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")
            val content = fetchSnippetContent(id)
            return translate(snippet, content, permission, snippet.compliance)
        }

        fun createSnippet(
            snippetDto: CreateSnippetDto,
            jwt: Jwt,
        ): SnippetDto {
            validateSnippetContent(snippetDto.content)

            val snippet = translate(snippetDto)
            val savedSnippet = repository.save(snippet)

            lintService.lintSnippet(savedSnippet.id!!, jwt.subject)
            formatService.formatSnippet(savedSnippet.id!!, jwt.subject)

            assetService.createAsset("snippets", savedSnippet.id.toString(), snippetDto.content).block()
            val permission = permissionService.addPermission(jwt, savedSnippet.id!!, "OWNER").block()!!
            return translate(savedSnippet, snippetDto.content, permission, savedSnippet.compliance)
        }

        fun createFromFile(
            snippetDto: CreateSnippetFileDto,
            file: MultipartFile,
            jwt: Jwt,
        ): SnippetDto {
            val content = file.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            validateSnippetContent(content)
            val snippet = translate(snippetDto)
            val savedSnippet = repository.save(snippet)
            val permission = permissionService.addPermission(jwt, savedSnippet.id!!, "OWNER").block()!!
            assetService.createAsset("snippets", savedSnippet.id.toString(), content).block()
            savedSnippet.id?.let { permissionService.addPermission(jwt, it, "owner") }
            lintService.lintSnippet(savedSnippet.id!!, jwt.subject)
            formatService.formatSnippet(savedSnippet.id!!, jwt.subject)
            return translate(savedSnippet, content, permission, savedSnippet.compliance)
        }

        fun editSnippet(
            snippetDto: CreateSnippetDto,
            id: Long,
            jwt: Jwt,
        ): SnippetDto {
            validateSnippetContent(snippetDto.content)
            val canModify = permissionService.canModify(jwt, id).block() ?: false
            if (!canModify) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
            }

            val permission =
                permissionService.getPermission(jwt, id).block()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")
            assetService.createAsset("snippets", id.toString(), snippetDto.content).block()

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

        fun editFromFile(
            snippetDto: CreateSnippetFileDto,
            file: MultipartFile,
            id: Long,
            jwt: Jwt,
        ): SnippetDto {
            val content = file.inputStream.bufferedReader(Charsets.UTF_8).use { it.readText() }
            validateSnippetContent(content)
            val canModify = permissionService.canModify(jwt, id).block() ?: false
            if (!canModify) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
            }

            val permission =
                permissionService.getPermission(jwt, id).block()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")

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

            assetService.createAsset("snippets", savedSnippet.id.toString(), content).block()

            lintService.lintSnippet(savedSnippet.id!!, jwt.subject)
            formatService.formatSnippet(savedSnippet.id!!, jwt.subject)

            return translate(savedSnippet, content, permission, savedSnippet.compliance)
        }

        @Transactional
        fun deleteSnippet(
            id: Long,
            jwt: Jwt,
        ) {
            val canModify = permissionService.canModify(jwt, id).block() ?: false
            if (!canModify) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
            }
            repository.deleteById(id)
            permissionService.removePermission(jwt, id, "OWNER").block()
            assetService.deleteAsset("snippets", id.toString()).block()
        }

        private fun fetchSnippetContent(id: Long): String {
            return assetService.getAsset("snippets", id.toString()).block()
                ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet content not found")
        }

        private fun validateSnippetContent(content: String) {
            val validation =
                printScriptService.validate(content).block()
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating snippet")
            if (!validation.ok) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid snippet content")
            }
        }

        fun getSnippetsByUser(
            jwt: Jwt,
            pageable: Pageable,
        ): Page<SnippetDto> {
            val permissions = permissionService.getAllSnippetPermissions(jwt).collectList().block() ?: emptyList()
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
            val canModify = permissionService.canModify(jwt, id).block() ?: false
            if (!canModify) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
            }

            val permissionResponse =
                permissionService.getPermission(jwt, id).block()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Permission not found")

            val originalSnippet =
                repository.findSnippetById(id)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")

            assetService.createAsset("snippets", id.toString(), snippet).block()

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

        fun updateLintingCompliance(
            snippetId: Long,
            compliance: Boolean,
        ) {
            val snippet = repository.findSnippetById(snippetId)
            val complianceAssigned = if (compliance) Compliance.COMPLIANT else Compliance.NON_COMPLIANT
            snippet?.let {
                repository.save(
                    Snippet(
                        id = snippet.id,
                        name = snippet.name,
                        description = snippet.description,
                        language = snippet.language,
                        compliance = complianceAssigned,
                        extension = snippet.extension,
                    ),
                )
            }
        }

        private fun translate(snippetDto: CreateSnippetDto) =
            Snippet(
                name = snippetDto.name,
                description = snippetDto.description,
                language = snippetDto.language,
                compliance = Compliance.PENDING,
                extension = snippetDto.extension,
            )

        private fun translate(snippetFileDto: CreateSnippetFileDto) =
            Snippet(
                name = snippetFileDto.name,
                description = snippetFileDto.description,
                language = snippetFileDto.language,
                compliance = Compliance.PENDING,
                extension = snippetFileDto.extension,
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
            username = permissionResponse.username,
            compliance = compliance.toString(),
        )
    }
