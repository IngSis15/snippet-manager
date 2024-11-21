package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.format.FormatService
import edu.ingsis.snippetmanager.lint.LintService
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.StatusDto
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
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.argumentCaptor
import org.mockito.quality.Strictness
import org.springframework.data.domain.PageRequest
import org.springframework.security.oauth2.jwt.Jwt
import reactor.core.publisher.Flux
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
        val snippet = Snippet(1L, "Snippet1", "Description1", "printscript", Compliance.PENDING, "ps")

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
        val snippet = Snippet(1L, "Snippet1", "Description1", "printscript", Compliance.PENDING, "ps")

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
        val snippet = Snippet(1L, "Snippet1", "Description1", "printscript", Compliance.PENDING, "ps")
        val editedSnippetDto =
            CreateSnippetDto(
                name = snippet.name,
                description = snippet.description,
                language = snippet.language,
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

    @Test
    fun `can create snippet`() {
        val snippetDto =
            CreateSnippetDto(
                name = "Declaration",
                description = "This snippet declares a variable y",
                language = "printscript",
                content = "let y: number = 10;",
                extension = "ps",
            )

        val permissionResponseDTO = PermissionResponseDTO("1", "test-user", 1L, "owner", "test user")

        val validateResultDTO = ValidateResultDTO(true, emptyList())

        `when`(
            repository.save(anyOrNull()),
        ).thenReturn(Snippet(1L, snippetDto.name, snippetDto.description, snippetDto.language, Compliance.PENDING, snippetDto.extension))
        `when`(permissionService.addPermission(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Mono.just(permissionResponseDTO))
        `when`(assetService.createAsset(anyOrNull(), anyOrNull(), anyOrNull())).thenReturn(Mono.empty())
        `when`(printScriptService.validate(anyOrNull())).thenReturn(Mono.just(validateResultDTO))

        val result = snippetService.createSnippet(snippetDto, jwtToken)

        assertEquals(snippetDto.content, result.content)
    }

    @Test
    fun `can get snippets by user`() {
        // Arrange
        val permission1 = PermissionResponseDTO("1", "test-user", 1L, "owner", "test user")
        val permission2 = PermissionResponseDTO("2", "test-user", 2L, "owner", "test user")
        val permissions = listOf(permission1, permission2)
        val fluxPermissions = Flux.fromIterable(permissions)

        `when`(permissionService.getAllSnippetPermissions(anyOrNull())).thenReturn(fluxPermissions)

        val snippet1 = Snippet(1L, "Snippet1", "Description1", "printscript", Compliance.PENDING, "ps")
        val snippet2 = Snippet(2L, "Snippet2", "Description2", "printscript", Compliance.PENDING, "ps")

        `when`(repository.findSnippetById(1L)).thenReturn(snippet1)
        `when`(repository.findSnippetById(2L)).thenReturn(snippet2)

        `when`(assetService.getAsset("snippets", "1")).thenReturn(Mono.just("content1"))
        `when`(assetService.getAsset("snippets", "2")).thenReturn(Mono.just("content2"))

        // Act
        val pageable = PageRequest.of(0, 10)
        val result = snippetService.getSnippetsByUser(jwtToken, pageable)

        // Assert
        assertEquals(2L, result.totalElements)
        assertEquals(2, result.content.size)
        assertEquals("Snippet1", result.content[0].name)
        assertEquals("Snippet2", result.content[1].name)
        assertEquals("content1", result.content[0].content)
        assertEquals("content2", result.content[1].content)
    }

    @Test
    fun `can update snippet from string successfully`() {
        // Arrange
        val snippetId = 1L
        val snippetContent = "let x = 10;"

        `when`(permissionService.canModify(jwtToken, snippetId)).thenReturn(Mono.just(true))

        val permissionResponseDTO = PermissionResponseDTO("1", jwtToken.subject, snippetId, "owner", "test user")
        `when`(permissionService.getPermission(jwtToken, snippetId)).thenReturn(Mono.just(permissionResponseDTO))

        val originalSnippet = Snippet(snippetId, "Snippet1", "Description1", "printscript", Compliance.COMPLIANT, "ps")
        `when`(repository.findSnippetById(snippetId)).thenReturn(originalSnippet)

        `when`(assetService.createAsset("snippets", snippetId.toString(), snippetContent)).thenReturn(Mono.empty())

        val savedSnippet =
            Snippet(
                snippetId,
                originalSnippet.name,
                originalSnippet.description,
                originalSnippet.language,
                Compliance.PENDING,
                originalSnippet.extension,
            )
        `when`(repository.save(anyOrNull())).thenReturn(savedSnippet)

        `when`(lintService.lintSnippet(snippetId, jwtToken.subject)).thenAnswer { }
        `when`(formatService.formatSnippet(snippetId, jwtToken.subject)).thenAnswer { }

        val validateResultDTO = ValidateResultDTO(true, emptyList())
        `when`(printScriptService.validate(anyOrNull())).thenReturn(Mono.just(validateResultDTO))

        // Act
        val result = snippetService.updateFromString(snippetContent, jwtToken, snippetId)

        // Assert
        assertEquals(savedSnippet.id, result.id)
        assertEquals(savedSnippet.name, result.name)
        assertEquals(savedSnippet.description, result.description)
        assertEquals(savedSnippet.language, result.language)
        assertEquals(snippetContent, result.content)
        assertEquals(Compliance.PENDING.toString(), result.compliance)
    }

    @Test
    fun `updateLintingCompliance updates compliance correctly`() {
        // Arrange
        val snippetId = 1L
        val statusDto = StatusDto(snippetId, Compliance.COMPLIANT)
        val snippet =
            Snippet(
                id = snippetId,
                name = "Snippet1",
                description = "Description1",
                language = "printscript",
                compliance = Compliance.PENDING,
                extension = "ps",
            )

        `when`(repository.findSnippetById(snippetId)).thenReturn(snippet)
        `when`(repository.save(any())).thenReturn(snippet)

        // Act
        snippetService.updateLintingCompliance(statusDto)

        // Assert
        val snippetCaptor = argumentCaptor<Snippet>()
        verify(repository).save(snippetCaptor.capture())
        val savedSnippet = snippetCaptor.firstValue

        assertEquals(snippetId, savedSnippet.id)
        assertEquals(Compliance.COMPLIANT, savedSnippet.compliance)
        assertEquals(snippet.name, savedSnippet.name)
        assertEquals(snippet.description, savedSnippet.description)
        assertEquals(snippet.language, savedSnippet.language)
        assertEquals(snippet.extension, savedSnippet.extension)
    }

    @Test
    fun `formatSnippet returns formatted content`() {
        // Arrange
        val snippetId = 1L
        val formattedContent = "formatted snippet content"

        `when`(assetService.getAsset("formatted", snippetId.toString())).thenReturn(Mono.just(formattedContent))

        // Act
        val result = snippetService.formatSnippet(snippetId)

        // Assert
        assertEquals(formattedContent, result)
        verify(assetService).getAsset("formatted", snippetId.toString())
    }
}
