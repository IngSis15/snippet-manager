package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.format.FormatService
import edu.ingsis.snippetmanager.lint.LintService
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.eq
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.junit.jupiter.MockitoSettings
import org.mockito.kotlin.anyOrNull
import org.mockito.quality.Strictness
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Mono
import java.time.Instant

@ExtendWith(MockitoExtension::class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SnippetServiceTests {
    @Mock
    private lateinit var repository: SnippetRepository

    @Mock
    private lateinit var printScriptService: PrintScriptApi

    @Mock
    private lateinit var assetService: AssetApi

    @Mock
    private lateinit var permissionService: PermissionService

    @Mock
    private lateinit var lintService: LintService

    @Mock
    private lateinit var formatService: FormatService

    @InjectMocks
    private lateinit var snippetService: SnippetService

    private lateinit var jwtToken: Jwt

    @BeforeEach
    fun setUp() {
        jwtToken =
            Jwt.withTokenValue("mockedToken")
                .header("alg", "none")
                .claim("sub", "test-user")
                .claim("scope", "read:snippets write:snippets")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plusSeconds(3600))
                .build()
    }

    @Test
    fun `can get snippet from id`() {
        val snippet = Snippet(1L, "Snippet1", "Description1", "printscript", "1.1", "ps")

        val permissionResponseDTO = PermissionResponseDTO("1", "test-user", snippet.id!!, "owner", "test user")

        `when`(repository.findSnippetById(snippet.id!!)).thenReturn(snippet)
        `when`(assetService.getAsset(anyOrNull(), anyOrNull())).thenReturn(Mono.just("content"))
        `when`(permissionService.canRead(anyOrNull(), anyOrNull())).thenReturn(Mono.just(true))
        `when`(permissionService.getPermission(anyOrNull(), anyOrNull())).thenReturn(Mono.just(permissionResponseDTO))

        val foundSnippet = snippetService.getSnippet(snippet.id!!, jwtToken)

        assertEquals(snippet.name, foundSnippet.name)
    }

    @Test
    fun `can delete snippet`() {
        val snippet = Snippet(1L, "Snippet1", "Description1", "printscript", "1.1", "ps")

        `when`(repository.findSnippetById(snippet.id!!)).thenReturn(snippet)
        `when`(permissionService.canModify(anyOrNull(), anyOrNull())).thenReturn(Mono.just(true))
        doNothing().`when`(repository).deleteById(anyOrNull())
        `when`(permissionService.removePermission(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Mono.empty())
        `when`(assetService.deleteAsset(anyOrNull(), anyOrNull())).thenReturn(Mono.empty())

        snippetService.deleteSnippet(snippet.id!!, jwtToken)

        verify(repository, times(1)).deleteById(anyOrNull())
    }

    @Test
    fun `can edit snippet`() {
        val snippet = Snippet(1L, "Snippet1", "Description1", "printscript", "1.1", "ps")
        val editedSnippetDto =
            CreateSnippetDto(
                name = snippet.name,
                description = snippet.description,
                language = snippet.language,
                version = snippet.version,
                extension = snippet.extension,
                content = "let a: number = 1;",
            )

        val permissionResponseDTO = PermissionResponseDTO("1", "test-user", snippet.id!!, "owner", "test user")

        val validateResultDTO = ValidateResultDTO(true, emptyList())

        `when`(repository.findSnippetById(snippet.id!!)).thenReturn(snippet)
        `when`(repository.save(anyOrNull())).thenReturn(snippet)
        `when`(permissionService.canModify(anyOrNull(), eq(snippet.id!!))).thenReturn(Mono.just(true))
        `when`(assetService.createAsset(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Mono.empty())
        `when`(printScriptService.validate(anyOrNull())).thenReturn(Mono.just(validateResultDTO))
        `when`(permissionService.getPermission(anyOrNull(), anyOrNull())).thenReturn(Mono.just(permissionResponseDTO))

        val result = snippetService.editSnippet(editedSnippetDto, snippet.id!!, jwtToken)

        assertEquals(editedSnippetDto.content, result.content)
    }
}
