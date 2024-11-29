package edu.ingsis.snippetmanager.config
import edu.ingsis.snippetmanager.SnippetManagerApplication
import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import edu.ingsis.snippetmanager.external.asset.AssetApi
import edu.ingsis.snippetmanager.external.permission.PermissionService
import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import edu.ingsis.snippetmanager.format.FormatService
import edu.ingsis.snippetmanager.format.FormatSnippetProducer
import edu.ingsis.snippetmanager.lint.LintService
import edu.ingsis.snippetmanager.lint.LintSnippetProducer
import edu.ingsis.snippetmanager.security.OAuth2ResourceServerSecurityConfiguration
import edu.ingsis.snippetmanager.test.TestService
import org.junit.jupiter.api.BeforeEach
import org.mockito.ArgumentMatchers.anyString
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.eq
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.web.server.ResponseStatusException
import kotlin.test.Test

@ContextConfiguration(classes = [SnippetManagerApplication::class])
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ConfigE2ETests {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var auth: OAuth2ResourceServerSecurityConfiguration

    @MockBean
    lateinit var assetService: AssetApi

    @MockBean
    lateinit var permissionService: PermissionService

    @MockBean
    lateinit var lintSnippetProducer: LintSnippetProducer

    @MockBean
    lateinit var formatSnippetProducer: FormatSnippetProducer

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
    fun `should lint snippets`() {
        val userId = "testUser"

        whenever(permissionService.getAllOwnerSnippetPermissions(anyOrNull())).thenReturn(
            listOf(
                PermissionResponseDTO("1", "testUser", 1L, "OWNER", "testUser"),
            ),
        )
        whenever(assetService.createAsset(eq("linting"), eq(userId), anyString())).thenAnswer { }

        doNothing().whenever(lintSnippetProducer).publishEvent(anyOrNull())

        mockMvc.perform(
            put("/v1/config/linting")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {"identifier_format":"camel case",
                    "mandatory-variable-or-literal-in-println":true,
                    "mandatory-variable-or-literal-in-readInput":true}
                    """.trimMargin(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier_format").value("camel case"))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-println").value(true))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-readInput").value(true))

        verify(lintSnippetProducer, times(1)).publishEvent(any())
    }

    @Test
    fun `should format snippets`() {
        val userId = "testUser"

        whenever(permissionService.getAllOwnerSnippetPermissions(anyOrNull())).thenReturn(
            listOf(
                PermissionResponseDTO("1", "testUser", 1L, "OWNER", "testUser"),
            ),
        )

        whenever(assetService.createAsset(eq("formatting"), eq(userId), anyString())).thenAnswer { }

        doNothing().whenever(formatSnippetProducer).publishEvent(any())

        mockMvc.perform(
            put("/v1/config/formatting")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(
                    """
                    {
                      "enforce-spacing-before-colon-in-declaration": false,
                      "enforce-spacing-after-colon-in-declaration": false,
                      "enforce-no-spacing-around-equals": true,
                      "newLinesBeforePrintln": 0,
                      "indent-inside-if": 4
                    }
                    """.trimIndent(),
                ),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.enforce-spacing-before-colon-in-declaration").value(false))
            .andExpect(jsonPath("$.enforce-spacing-after-colon-in-declaration").value(false))
            .andExpect(jsonPath("$.enforce-no-spacing-around-equals").value(true))
            .andExpect(jsonPath("$.newLinesBeforePrintln").value(0))
            .andExpect(jsonPath("$.indent-inside-if").value(4))

        verify(formatSnippetProducer, times(1)).publishEvent(any())
    }

    @Test
    fun `should return existing linting configuration for user`() {
        val userId = "testUser"
        val existingConfig =
            LintingSchemaDTO(
                casingFormat = "camel case",
                expressionAllowedInPrint = false,
                expressionAllowedInReadInput = false,
            )
        val existingConfigJson =
            """
            {
                "identifier_format": "camel case",
                "mandatory-variable-or-literal-in-println": false,
                "mandatory-variable-or-literal-in-readInput": false
            }
            """.trimIndent()

        whenever(assetService.getAsset(eq("linting"), eq(userId))).thenReturn(existingConfigJson)

        mockMvc.perform(
            get("/v1/config/linting")
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier_format").value("camel case"))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-println").value(false))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-readInput").value(false))

        verify(assetService, times(1)).getAsset(eq("linting"), eq(userId))
        verifyNoInteractions(permissionService, lintSnippetProducer, formatSnippetProducer)
    }

    @Test
    fun `should return default linting configuration when user has no config`() {
        val userId = "testUser"

        whenever(assetService.getAsset(eq("linting"), eq(userId))).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
        whenever(assetService.createAsset(eq("linting"), eq(userId), anyString())).thenAnswer { }

        mockMvc.perform(
            get("/v1/config/linting")
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier_format").value("camel case"))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-println").value(true))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-readInput").value(true))

        verify(assetService, times(1)).getAsset(eq("linting"), eq(userId))
        verify(assetService, times(1)).createAsset(eq("linting"), eq(userId), anyString())
    }

    @Test
    fun `should return existing formatting configuration for user`() {
        val userId = "testUser"
        val existingConfig =
            FormattingSchemaDTO(
                spaceBeforeColon = false,
                spaceAfterColon = true,
                noSpaceAroundAssignment = false,
                newLinesBeforePrintln = 2,
                indentSpaces = 4,
            )
        val existingConfigJson =
            """
            {
                "enforce-spacing-before-colon-in-declaration": false,
                "enforce-spacing-after-colon-in-declaration": true,
                "enforce-no-spacing-around-equals": false,
                "newLinesBeforePrintln": 2,
                "indent-inside-if": 4
            }
            """.trimIndent()

        whenever(assetService.getAsset(eq("formatting"), eq(userId))).thenReturn(existingConfigJson)

        mockMvc.perform(
            get("/v1/config/formatting")
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.enforce-spacing-before-colon-in-declaration").value(false))
            .andExpect(jsonPath("$.enforce-spacing-after-colon-in-declaration").value(true))
            .andExpect(jsonPath("$.enforce-no-spacing-around-equals").value(false))
            .andExpect(jsonPath("$.newLinesBeforePrintln").value(2))
            .andExpect(jsonPath("$.indent-inside-if").value(4))

        verify(assetService, times(1)).getAsset(eq("formatting"), eq(userId))
        verifyNoInteractions(permissionService, lintSnippetProducer, formatSnippetProducer)
    }

    @Test
    fun `should return default formatting configuration when user has no config`() {
        val userId = "testUser"

        whenever(assetService.getAsset(eq("formatting"), eq(userId))).thenThrow(ResponseStatusException(HttpStatus.NOT_FOUND))
        whenever(assetService.createAsset(eq("formatting"), eq(userId), anyString())).thenAnswer { }

        mockMvc.perform(
            get("/v1/config/formatting")
                .header("Authorization", "Bearer mock-token"),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.enforce-spacing-before-colon-in-declaration").value(false))
            .andExpect(jsonPath("$.enforce-spacing-after-colon-in-declaration").value(false))
            .andExpect(jsonPath("$.enforce-no-spacing-around-equals").value(true))
            .andExpect(jsonPath("$.newLinesBeforePrintln").value(0))
            .andExpect(jsonPath("$.indent-inside-if").value(4))

        verify(assetService, times(1)).getAsset(eq("formatting"), eq(userId))
        verify(assetService, times(1)).createAsset(eq("formatting"), eq(userId), anyString())
    }

    @Test
    fun `should save linting configuration for user`() {
        val userId = "testUser"
        val lintingConfig =
            """
            {
                "identifier_format": "snake case",
                "mandatory-variable-or-literal-in-println": true,
                "mandatory-variable-or-literal-in-readInput": false
            }
            """.trimIndent()

        whenever(permissionService.getAllOwnerSnippetPermissions(anyOrNull())).thenReturn(
            listOf(
                PermissionResponseDTO("1", "testUser", 1L, "OWNER", "testUser"),
            ),
        )

        doNothing().whenever(lintSnippetProducer).publishEvent(anyOrNull())

        // Mock asset service behavior
        whenever(assetService.createAsset(eq("linting"), eq(userId), anyString())).thenAnswer { }

        // Perform PUT request
        mockMvc.perform(
            put("/v1/config/linting")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(lintingConfig),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.identifier_format").value("snake case"))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-println").value(true))
            .andExpect(jsonPath("$.mandatory-variable-or-literal-in-readInput").value(false))

        // Verify interactions
        verify(assetService, times(1)).createAsset(eq("linting"), eq(userId), anyString())
    }

    @Test
    fun `should save formatting configuration for user`() {
        val userId = "testUser"
        val formattingConfig =
            """
            {
                "enforce-spacing-before-colon-in-declaration": true,
                "enforce-spacing-after-colon-in-declaration": false,
                "enforce-no-spacing-around-equals": true,
                "newLinesBeforePrintln": 15,
                "indent-inside-if": 3
            }
            """.trimIndent()

        whenever(permissionService.getAllOwnerSnippetPermissions(anyOrNull())).thenReturn(
            listOf(
                PermissionResponseDTO("1", "testUser", 1L, "OWNER", "testUser"),
            ),
        )

        doNothing().whenever(formatSnippetProducer).publishEvent(any())

        // Mock asset service behavior
        whenever(assetService.createAsset(eq("formatting"), eq(userId), anyString())).thenAnswer { }

        // Perform PUT request
        mockMvc.perform(
            put("/v1/config/formatting")
                .header("Authorization", "Bearer mock-token")
                .contentType(MediaType.APPLICATION_JSON)
                .content(formattingConfig),
        )
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.enforce-spacing-before-colon-in-declaration").value(true))
            .andExpect(jsonPath("$.enforce-spacing-after-colon-in-declaration").value(false))
            .andExpect(jsonPath("$.enforce-no-spacing-around-equals").value(true))
            .andExpect(jsonPath("$.newLinesBeforePrintln").value(15))
            .andExpect(jsonPath("$.indent-inside-if").value(3))

        // Verify interactions
        verify(assetService, times(1)).createAsset(eq("formatting"), eq(userId), anyString())
    }
}
