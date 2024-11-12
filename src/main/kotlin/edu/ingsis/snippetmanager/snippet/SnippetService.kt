package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetFileDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
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
    ) {
        fun getSnippet(
            id: Long,
            jwt: Jwt,
        ): SnippetDto {
            val canRead = permissionService.canRead(jwt, id).block() ?: false
            if (!canRead) {
                throw ResponseStatusException(HttpStatus.FORBIDDEN, "Permission denied")
            }
            val snippet =
                repository.findSnippetById(id)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")
            val content = fetchSnippetContent(id)
            return translate(snippet, content, "READ")
        }

        fun createSnippet(
            snippetDto: CreateSnippetDto,
            jwt: Jwt,
        ): SnippetDto {
            validateSnippetContent(snippetDto.content)

            val snippet = translate(snippetDto)
            val savedSnippet = repository.save(snippet)
            assetService.createAsset("snippets", savedSnippet.id.toString(), snippetDto.content).block()
            permissionService.addPermission(jwt, savedSnippet.id!!, "OWNER").block()
            return translate(savedSnippet, snippetDto.content, "OWNER")
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
            assetService.createAsset("snippets", savedSnippet.id.toString(), content).block()
            savedSnippet.id?.let { permissionService.addPermission(jwt, it, "owner") }
            return translate(savedSnippet, content, "owner")
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

            assetService.createAsset("snippets", id.toString(), snippetDto.content).block()

            val savedSnippet =
                repository.save(
                    Snippet(
                        id,
                        snippetDto.name,
                        snippetDto.description,
                        snippetDto.language,
                        snippetDto.version,
                        snippetDto.extension,
                        Conformance.PENDING,
                    ),
                )

            return translate(savedSnippet, snippetDto.content, "OWNER")
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

            val savedSnippet =
                repository.save(
                    Snippet(
                        id,
                        snippetDto.name,
                        snippetDto.description,
                        snippetDto.language,
                        snippetDto.version,
                        snippetDto.extension,
                        Conformance.PENDING,
                    ),
                )
            assetService.createAsset("snippets", savedSnippet.id.toString(), content).block()

            return translate(savedSnippet, content, "OWNER")
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

        fun getSnippetsByUser(jwt: Jwt): List<SnippetDto> {
            return permissionService.getAllSnippetPermissions(jwt).toIterable().map {
                    permission ->
                val snippet =
                    repository.findSnippetById(permission.snippetId)
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")
                val content = fetchSnippetContent(permission.snippetId)
                translate(snippet, content, permission.permissionType)
            }
        }

        fun getAllSnippets(): List<SnippetDto> {
            return repository.findAll().map {
                val content = fetchSnippetContent(it.id!!)
                translate(it, content, "READ")
            }
        }

        private fun translate(snippetDto: CreateSnippetDto) =
            Snippet(
                name = snippetDto.name,
                description = snippetDto.description,
                language = snippetDto.language,
                version = snippetDto.version,
                extension = snippetDto.extension,
                conformance = Conformance.PENDING,
            )

        private fun translate(snippetFileDto: CreateSnippetFileDto) =
            Snippet(
                name = snippetFileDto.name,
                description = snippetFileDto.description,
                language = snippetFileDto.language,
                version = snippetFileDto.version,
                extension = snippetFileDto.extension,
                conformance = Conformance.PENDING,
            )

        private fun translate(
            snippet: Snippet,
            content: String,
            permission: String,
        ) = SnippetDto(
            id = snippet.id,
            name = snippet.name,
            description = snippet.description,
            language = snippet.language,
            version = snippet.version,
            content = content,
            extension = snippet.extension,
            permission = permission,
        )
    }
