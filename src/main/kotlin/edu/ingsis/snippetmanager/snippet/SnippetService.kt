package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateDTO
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetFileDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import jakarta.transaction.Transactional
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
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
    ) {
        fun getAllSnippets(): List<SnippetDto> {
            return repository.findAll().map {
                val snippetContent =
                    assetService.getAsset("snippets", it.id.toString()).block()
                        ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")

                translate(it, snippetContent)
            }
        }

        fun getSnippet(id: Long): SnippetDto {
            val snippetContent =
                assetService.getAsset("snippets", id.toString()).block()
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")

            val snippet =
                repository.findSnippetById(id)
                    ?: throw ResponseStatusException(HttpStatus.NOT_FOUND, "Snippet not found")

            return translate(snippet, snippetContent)
        }

        fun createSnippet(snippetDto: CreateSnippetDto): SnippetDto {
            val snippet = translate(snippetDto)

            val validation =
                printScriptService.validate(ValidateDTO(snippetDto.content, snippet.version)).block()
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating snippet")

            if (!validation.ok) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid snippet")
            }

            val savedSnippet = repository.save(snippet)
            assetService.createAsset("snippets", savedSnippet.id.toString(), snippetDto.content).block()

            return translate(savedSnippet, snippetDto.content)
        }

        fun createFromFile(
            snippetDto: CreateSnippetFileDto,
            file: MultipartFile,
        ): SnippetDto {
            val snippet = translate(snippetDto)
            val content = file.inputStream.readBytes().toString(Charsets.UTF_8)

            val validation =
                printScriptService.validate(ValidateDTO(content, snippet.version)).block()
                    ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error validating snippet")

            if (!validation.ok) {
                throw ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid snippet")
            }

            val savedSnippet = repository.save(snippet)
            assetService.createAsset("snippets", savedSnippet.id.toString(), content).block()

            return translate(savedSnippet, content)
        }

        @Transactional
        fun deleteSnippet(id: Long) {
            return repository.deleteById(id)
        }

        private fun translate(snippet: CreateSnippetDto): Snippet {
            return Snippet(
                name = snippet.name,
                description = snippet.description,
                language = snippet.language,
                version = snippet.version,
                extension = snippet.extension,
            )
        }

        private fun translate(
            snippet: Snippet,
            content: String,
        ): SnippetDto {
            return SnippetDto(
                id = snippet.id,
                name = snippet.name,
                description = snippet.description,
                language = snippet.language,
                version = snippet.version,
                content = content,
                extension = snippet.extension,
            )
        }

        private fun translate(snippetFile: CreateSnippetFileDto): Snippet {
            return Snippet(
                name = snippetFile.name,
                description = snippetFile.description,
                language = snippetFile.language,
                version = snippetFile.version,
                extension = snippetFile.extension,
            )
        }
    }
