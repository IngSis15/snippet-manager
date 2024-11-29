import edu.ingsis.snippetmanager.SnippetManagerApplication
import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.format.FormatService
import edu.ingsis.snippetmanager.lint.LintService
import edu.ingsis.snippetmanager.security.OAuth2ResourceServerSecurityConfiguration
import edu.ingsis.snippetmanager.snippet.Compliance
import edu.ingsis.snippetmanager.snippet.Snippet
import edu.ingsis.snippetmanager.snippet.SnippetRepository
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.ExecuteResultDto
import edu.ingsis.snippetmanager.test.TestService
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyLong
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.content
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status

@ContextConfiguration(classes = [SnippetManagerApplication::class])
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SnippetE2ETests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var auth: OAuth2ResourceServerSecurityConfiguration

    @MockBean
    lateinit var snippetRepository: SnippetRepository

    @MockBean
    lateinit var permissionService: PermissionService

    @MockBean
    lateinit var assetService: AssetApi

    @MockBean
    lateinit var printScriptService: PrintScriptApi

    @MockBean
    lateinit var lintService: LintService

    @MockBean
    lateinit var formatService: FormatService

    @MockBean
    lateinit var testService: TestService

    @BeforeEach
    fun setupSecurityContext() {
        val jwt =
            Jwt.withTokenValue("mock-token")
                .header("alg", "none")
                .claim("sub", "testUser")
                .build()

        val authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.getContext().authentication = authentication
    }

    @Test
    fun `should return snippet by ID when user has permission`() {
        val snippetId = 1L
        val snippet = Snippet(snippetId, "Snippet Name", "Description", "Language", Compliance.PENDING, ".ts")
        val permission = PermissionResponseDTO("1", "testUser", snippetId, "OWNER", "testUser")
        val snippetContent = "Snippet Content"

        whenever(permissionService.canRead(anyOrNull(), eq(snippetId))).thenReturn((true))
        whenever(permissionService.getPermission(anyOrNull(), eq(snippetId))).thenReturn((permission))
        whenever(snippetRepository.findSnippetById(snippetId)).thenReturn(snippet)
        whenever(assetService.getAsset("snippets", snippetId.toString())).thenReturn((snippetContent))

        mockMvc.perform(
            get("/v1/snippet/{id}", snippetId)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(snippetId))
            .andExpect(jsonPath("$.name").value("Snippet Name"))
    }

    @Test
    fun `should return 403 when user does not have permission`() {
        // Arrange: Mock permission response to deny access
        val snippetId = 1L
        whenever(permissionService.canRead(anyOrNull(), eq(snippetId))).thenReturn((false))

        // Act: Perform GET request on the endpoint
        mockMvc.perform(
            get("/v1/snippet/{id}", snippetId)
                .header("Authorization", "Bearer mock-token"),
        )
            // Assert: Validate HTTP 403 status
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return 404 when snippet is not found`() {
        // Arrange: Mock repository response for nonexistent snippet
        val snippetId = 1L
        whenever(permissionService.canRead(anyOrNull(), eq(snippetId))).thenReturn((true))
        whenever(
            permissionService.getPermission(anyOrNull(), eq(snippetId)),
        ).thenReturn((PermissionResponseDTO("1", "testUser", snippetId, "OWNER", "testUser")))
        whenever(snippetRepository.findSnippetById(snippetId)).thenReturn(null)

        // Act: Perform GET request on the endpoint
        mockMvc.perform(
            get("/v1/snippet/{id}", snippetId)
                .header("Authorization", "Bearer mock-token"),
        )
            // Assert: Validate HTTP 404 status
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should create a new snippet successfully`() {
        // Arrange: Mock dependencies
        val createSnippetDto =
            CreateSnippetDto(
                name = "Test Snippet",
                description = "Test Description",
                content = "println('Hello, world!')",
                language = "printScript",
                extension = ".ps",
            )

        val savedSnippet =
            Snippet(
                id = 1L,
                name = createSnippetDto.name,
                description = createSnippetDto.description,
                language = createSnippetDto.language,
                compliance = Compliance.PENDING,
                extension = createSnippetDto.extension,
            )

        val permissionResponse = PermissionResponseDTO("1", "testUser", savedSnippet.id!!, "OWNER", "testUser")

        whenever(printScriptService.validate(createSnippetDto.content)).thenReturn((ValidateResultDTO(true, emptyList())))
        whenever(snippetRepository.save(anyOrNull())).thenReturn(savedSnippet)
        whenever(assetService.createAsset("snippets", "1", "println('Hello, world!')")).thenAnswer { }
        whenever(lintService.lintSnippet(1L, "testUser")).thenAnswer { }
        whenever(formatService.formatSnippet(1L, "testUser")).thenAnswer { }
        whenever(permissionService.addPermission(anyOrNull(), anyLong(), anyString())).thenReturn((permissionResponse))

        // Act: Perform POST request with MockMvc
        mockMvc.perform(
            post("/v1/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "Test Snippet",
                        "description": "Test Description",
                        "content": "println('Hello, world!')",
                        "language": "printScript",
                        "extension": ".ps"
                    }
                    """.trimIndent(),
                )
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.id").value(1L))
            .andExpect(jsonPath("$.name").value("Test Snippet"))
            .andExpect(jsonPath("$.description").value("Test Description"))
            .andExpect(jsonPath("$.language").value("printScript"))
    }

    @Test
    fun `should return 400 when snippet content is invalid`() {
        // Arrange: Mock dependencies
        val createSnippetDto =
            CreateSnippetDto(
                name = "Test Snippet",
                description = "Test Description",
                content = "println('Hello, world!')",
                language = "printScript",
                extension = ".ps",
            )

        whenever(printScriptService.validate(createSnippetDto.content)).thenReturn((ValidateResultDTO(false, listOf())))

        // Act: Perform POST request with MockMvc
        mockMvc.perform(
            post("/v1/snippet")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "Test Snippet",
                        "description": "Test Description",
                        "content": "println('Hello, world!')",
                        "language": "printScript",
                        "extension": ".ps"
                    }
                    """.trimIndent(),
                )
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should edit snippet successfully`() {
        val snippetId = 1L
        val snippetDto = CreateSnippetDto("Updated Name", "Updated Description", "printScript", "println('Updated')", ".ps")
        val permissionResponse = PermissionResponseDTO("1", "testUser", snippetId, "OWNER", "testUser")
        val savedSnippet =
            Snippet(snippetId, snippetDto.name, snippetDto.description, snippetDto.language, Compliance.PENDING, snippetDto.extension)

        whenever(printScriptService.validate(snippetDto.content)).thenReturn((ValidateResultDTO(true, emptyList())))
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn((true))
        whenever(permissionService.getPermission(anyOrNull(), eq(snippetId))).thenReturn((permissionResponse))
        whenever(assetService.createAsset("snippets", snippetId.toString(), snippetDto.content)).thenAnswer { }
        whenever(snippetRepository.save(anyOrNull())).thenReturn(savedSnippet)
        whenever(lintService.lintSnippet(eq(snippetId), anyString())).thenAnswer { }
        whenever(formatService.formatSnippet(eq(snippetId), anyString())).thenAnswer { }

        mockMvc.perform(
            post("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "Updated Name",
                        "description": "Updated Description",
                        "content": "println('Updated')",
                        "language": "printScript",
                        "extension": ".ps"
                    }
                    """.trimIndent(),
                )
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(snippetId))
            .andExpect(jsonPath("$.name").value("Updated Name"))
            .andExpect(jsonPath("$.description").value("Updated Description"))
            .andExpect(jsonPath("$.language").value("printScript"))
    }

    @Test
    fun `should return 400 for invalid snippet content`() {
        val snippetId = 1L
        val snippetDto = CreateSnippetDto("Invalid Name", "Description", "printScript", "invalid content", ".ps")

        whenever(printScriptService.validate(snippetDto.content)).thenReturn((ValidateResultDTO(false, emptyList())))

        mockMvc.perform(
            post("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "Invalid Name",
                        "description": "Description",
                        "content": "invalid content",
                        "language": "printScript",
                        "extension": ".ps"
                    }
                    """.trimIndent(),
                )
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isBadRequest)
    }

    @Test
    fun `should return 403 for insufficient permissions`() {
        val snippetId = 1L
        val snippetDto = CreateSnippetDto("Name", "Description", "printScript", "content", ".ps")

        whenever(printScriptService.validate(snippetDto.content)).thenReturn((ValidateResultDTO(true, emptyList())))
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn((false))

        mockMvc.perform(
            post("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "Name",
                        "description": "Description",
                        "content": "content",
                        "language": "printScript",
                        "extension": ".ps"
                    }
                    """.trimIndent(),
                )
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return 404 for missing permission`() {
        val snippetId = 1L
        val snippetDto = CreateSnippetDto("Name", "Description", "printScript", "content", ".ps")

        whenever(printScriptService.validate(snippetDto.content)).thenReturn(ValidateResultDTO(true, emptyList()))
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn(true)
        whenever(permissionService.getPermission(anyOrNull(), eq(snippetId))).thenReturn(null)

        mockMvc.perform(
            post("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "name": "Name",
                        "description": "Description",
                        "content": "content",
                        "language": "printScript",
                        "extension": ".ps"
                    }
                    """.trimIndent(),
                )
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should delete snippet successfully`() {
        val snippetId = 1L

        // Mock permissions and dependencies
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn(true)
        doNothing().whenever(snippetRepository).deleteById(snippetId)
        whenever(permissionService.removePermission(anyOrNull(), eq(snippetId), anyString())).thenReturn(null)
        whenever(assetService.deleteAsset("snippets", snippetId.toString())).thenAnswer { }

        mockMvc.perform(
            delete("/v1/snippet/{id}", snippetId)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should return 403 when user does not have permission to delete`() {
        val snippetId = 1L

        // Mock permission check to deny access
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn((false))

        mockMvc.perform(
            delete("/v1/snippet/{id}", snippetId)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return all snippets for the user`() {
        val page = 0
        val size = 2

        val permissions =
            listOf(
                PermissionResponseDTO("1", "testUser", 1L, "OWNER", "testUser"),
                PermissionResponseDTO("2", "testUser", 2L, "OWNER", "testUser"),
            )

        val snippet1 = Snippet(1L, "Snippet 1", "Description 1", "printScript", Compliance.PENDING, ".ps")
        val snippet2 = Snippet(2L, "Snippet 2", "Description 2", "printScript", Compliance.PENDING, ".ps")

        // Mock permission service
        whenever(permissionService.getAllSnippetPermissions(anyOrNull())).thenReturn(permissions)

        // Mock repository behavior
        whenever(snippetRepository.findSnippetById(1L)).thenReturn(snippet1)
        whenever(snippetRepository.findSnippetById(2L)).thenReturn(snippet2)

        // Mock fetching snippet content
        whenever(assetService.getAsset("snippets", "1")).thenReturn(("content1"))
        whenever(assetService.getAsset("snippets", "2")).thenReturn(("content2"))

        // Perform GET request
        mockMvc.perform(
            get("/v1/snippet/user")
                .param("page", page.toString())
                .param("size", size.toString())
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.content.size()").value(2))
            .andExpect(jsonPath("$.content[0].id").value(1L))
            .andExpect(jsonPath("$.content[0].name").value("Snippet 1"))
            .andExpect(jsonPath("$.content[1].id").value(2L))
            .andExpect(jsonPath("$.content[1].name").value("Snippet 2"))
    }

    @Test
    fun `should update snippet content successfully`() {
        val snippetId = 1L
        val updatedContent = "print('Updated content')"
        val originalSnippet =
            Snippet(
                id = snippetId,
                name = "Original Snippet",
                description = "Original Description",
                language = "printScript",
                compliance = Compliance.PENDING,
                extension = ".ps",
            )
        val permissionResponse = PermissionResponseDTO("1", "testUser", snippetId, "OWNER", "testUser")
        val savedSnippet =
            Snippet(
                id = snippetId,
                name = "Original Snippet",
                description = "Original Description",
                language = "printScript",
                compliance = Compliance.PENDING,
                extension = ".ps",
            )

        // Mock dependencies
        whenever(printScriptService.validate(updatedContent)).thenReturn((ValidateResultDTO(true, emptyList())))
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn((true))
        whenever(permissionService.getPermission(anyOrNull(), eq(snippetId))).thenReturn((permissionResponse))
        whenever(snippetRepository.findSnippetById(eq(snippetId))).thenReturn(originalSnippet)
        whenever(assetService.createAsset("snippets", snippetId.toString(), updatedContent)).thenAnswer { }
        whenever(snippetRepository.save(anyOrNull())).thenReturn(savedSnippet)
        whenever(lintService.lintSnippet(snippetId, "testUser")).thenAnswer { }
        whenever(formatService.formatSnippet(snippetId, "testUser")).thenAnswer { }

        // Perform PUT request
        mockMvc.perform(
            put("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(updatedContent)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.id").value(snippetId))
            .andExpect(jsonPath("$.name").value("Original Snippet"))
            .andExpect(jsonPath("$.description").value("Original Description"))
            .andExpect(jsonPath("$.language").value("printScript"))
            .andExpect(jsonPath("$.content").value(updatedContent))
            .andExpect(jsonPath("$.extension").value(".ps"))
            .andExpect(jsonPath("$.permission").value("OWNER"))
            .andExpect(jsonPath("$.author").value("testUser"))
            .andExpect(jsonPath("$.compliance").value("PENDING"))
    }

    @Test
    fun `should return 403 when user lacks permission to modify snippet`() {
        val snippetId = 1L
        val updatedContent = "println('Updated content')"

        whenever(printScriptService.validate(updatedContent)).thenReturn((ValidateResultDTO(true, emptyList())))
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn((false))

        mockMvc.perform(
            put("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(updatedContent)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isForbidden)
    }

    @Test
    fun `should return 404 when permission is not found`() {
        val snippetId = 1L

        val updatedContent = "println('Updated content')"

        whenever(printScriptService.validate(updatedContent)).thenReturn(ValidateResultDTO(true, emptyList()))
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn(true)
        whenever(permissionService.getPermission(anyOrNull(), eq(snippetId))).thenReturn(null)

        mockMvc.perform(
            put("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(updatedContent)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should return 404 when snippet is not found while editing from string`() {
        val snippetId = 1L

        val updatedContent = "println('Updated content')"

        whenever(printScriptService.validate(updatedContent)).thenReturn((ValidateResultDTO(true, emptyList())))
        whenever(permissionService.canModify(anyOrNull(), eq(snippetId))).thenReturn((true))
        whenever(
            permissionService.getPermission(anyOrNull(), eq(snippetId)),
        ).thenReturn((PermissionResponseDTO("1", "testUser", snippetId, "OWNER", "testUser")))
        whenever(snippetRepository.findSnippetById(eq(snippetId))).thenReturn(null)

        mockMvc.perform(
            put("/v1/snippet/{id}", snippetId)
                .contentType(MediaType.TEXT_PLAIN)
                .content(updatedContent)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isNotFound)
    }

    @Test
    fun `should update linting compliance successfully`() {
        val snippet =
            Snippet(
                id = 1L,
                name = "Snippet 1",
                description = "Description 1",
                language = "printScript",
                compliance = Compliance.PENDING,
                extension = ".ps",
            )

        // Mock repository behavior
        whenever(snippetRepository.findSnippetById(1L)).thenReturn(snippet)
        whenever(snippetRepository.save(anyOrNull())).thenReturn(
            Snippet(
                id = 1L,
                name = "Snippet 1",
                description = "Description 1",
                language = "printScript",
                compliance = Compliance.COMPLIANT,
                extension = ".ps",
            ),
        )

        // Perform POST request
        mockMvc.perform(
            post("/v1/snippet/status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                        "snippetId": 1,
                        "compliance": "COMPLIANT"
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
    }

    @Test
    fun `should return formatted snippet successfully`() {
        val snippetId = 1L
        val formattedContent = "Formatted snippet content"

        // Mock asset service behavior
        whenever(assetService.getAsset("formatted", snippetId.toString())).thenReturn((formattedContent))

        // Perform GET request
        mockMvc.perform(
            get("/v1/snippet/format/{id}", snippetId)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(content().string(formattedContent))
    }

    @Test
    fun `should run test for snippet successfully`() {
        val testId = 1L
        val snippetId = 10L

        val snippet =
            Snippet(
                id = snippetId,
                name = "Test Snippet",
                description = "Snippet used in a test",
                language = "printScript",
                compliance = Compliance.PENDING,
                extension = ".ps",
            )

        val testEntity =
            edu.ingsis.snippetmanager.test.Test(
                id = testId,
                name = "Sample Test",
                snippet = snippet,
                expectedOutput = listOf("Expected Output1", "Expected Output2"),
                userInput = listOf("User Input"),
            )

        whenever(testService.getTestById(testId)).thenReturn(testEntity)
        whenever(permissionService.canRead(anyOrNull(), eq(snippetId))).thenReturn((true))
        whenever(printScriptService.execute(snippetId, testEntity.userInput))
            .thenReturn((ExecuteResultDto(listOf("Expected Output1", "Expected Output2"))))

        // Perform GET request
        mockMvc.perform(
            get("/v1/snippet/test/{testId}", testId)
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.passed").value(true))
            .andExpect(jsonPath("$.expectedOutput").isArray)
            .andExpect(jsonPath("$.expectedOutput[0]").value("Expected Output1"))
            .andExpect(jsonPath("$.actualOutput[1]").value("Expected Output2"))
            .andExpect(jsonPath("$.actualOutput").isArray)
            .andExpect(jsonPath("$.actualOutput[0]").value("Expected Output1"))
            .andExpect(jsonPath("$.actualOutput[1]").value("Expected Output2"))
    }

    // Helper to mock Jwt token
    private fun mockJwt(): Jwt {
        return Jwt.withTokenValue("mock-token")
            .header("alg", "none")
            .claim("sub", "testUser")
            .build()
    }
}
