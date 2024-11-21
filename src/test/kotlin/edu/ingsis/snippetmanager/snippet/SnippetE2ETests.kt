package edu.ingsis.snippetmanager.snippet

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import edu.ingsis.snippetmanager.snippet.dto.StatusDto
import edu.ingsis.snippetmanager.snippet.dto.TestResponseDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.data.domain.PageImpl
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(SnippetRoutes::class)
class SnippetE2ETests
    @Autowired
    constructor(private val mockMvc: MockMvc, private val objectMapper: ObjectMapper) {
        @MockBean
        private lateinit var snippetService: SnippetService

        @MockBean
        private lateinit var testSnippetService: TestSnippetService

        private lateinit var jwtToken: Jwt

        @BeforeEach
        fun setUp() {
            jwtToken =
                Jwt.withTokenValue("mockedToken")
                    .header("alg", "none")
                    .claim(JwtClaimNames.SUB, "test-user")
                    .claim("scope", "read:snippets write:snippets")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build()
        }

        @Test
        fun `can get snippet from id`() {
            val snippet = SnippetFixtures.all().first()
            val snippetContent = "let y: number = 10;"

            mockMvc.get("/v1/snippet/${snippet.id}") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `can create snippet`() {
            val snippet =
                CreateSnippetDto(
                    name = "Declaration",
                    description = "This snippet declares a variable y",
                    language = "printscript",
                    content = "let y: number = 10;",
                    extension = "ps",
                )

            mockMvc.post("/v1/snippet") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(snippet)
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isCreated() }
                }
        }

        @Test
        fun `can delete snippet`() {
            val snippet = SnippetFixtures.all().first()
            val snippetId = snippet.id ?: throw IllegalArgumentException("Snippet ID is null")

            mockMvc.delete("/v1/snippet/$snippetId") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `can edit snippet`() {
            val snippet = SnippetFixtures.all().first()
            val editedSnippet =
                CreateSnippetDto(
                    name = snippet.name,
                    description = snippet.description,
                    language = snippet.language,
                    extension = snippet.extension,
                    content = "let a: number = 1;",
                )

            mockMvc.post("/v1/snippet/${snippet.id}") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(editedSnippet)
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `can get snippets by user`() {
            // Arrange
            val snippet1 = SnippetFixtures.all().first()
            val snippet2 = SnippetFixtures.all().last()
            val snippets = listOf(snippet1, snippet2)
            val snippetsPage =
                PageImpl(
                    snippets.map {
                        SnippetDto(
                            id = it.id,
                            name = "String",
                            description = "String",
                            language = "String",
                            compliance = "String",
                            extension = "String",
                            content = "String",
                            permission = "String",
                            author = "String",
                        )
                    },
                )

            `when`(snippetService.getSnippetsByUser(any(), any())).thenReturn(snippetsPage)

            // Act
            mockMvc.get("/v1/snippet") {
                param("page", "0")
                param("size", "10")
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.content.length()") { value(2) }
                    jsonPath("$.content[0].id") { value(snippet1.id) }
                    jsonPath("$.content[1].id") { value(snippet2.id) }
                    // Additional assertions can be added here
                }
        }

        @Test
        fun `can update snippet from string`() {
            // Arrange
            val snippetId = 1L
            val snippetContent = "let x = 10;"

            val updatedSnippet = SnippetFixtures.all().first()
            val updatedSnippetDto =
                SnippetDto(
                    id = updatedSnippet.id,
                    name = "String",
                    description = "String",
                    language = "String",
                    compliance = "String",
                    extension = "String",
                    content = snippetContent,
                    permission = "String",
                    author = "String",
                )

            `when`(snippetService.updateFromString(eq(snippetContent), any(), eq(snippetId))).thenReturn(updatedSnippetDto)

            // Act
            mockMvc.post("/v1/snippet/$snippetId/string") {
                contentType = MediaType.TEXT_PLAIN
                content = snippetContent
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.id") { value(snippetId) }
                    jsonPath("$.content") { value(snippetContent) }
                    // Additional assertions can be added here
                }
        }

        @Test
        fun `can run test`() {
            // Arrange
            val testId = 1L
            val testResponse = TestResponseDto(passed = true, expectedOutput = listOf("expected"), actualOutput = listOf("expected"))
            `when`(testSnippetService.runTest(eq(testId), any())).thenReturn(testResponse)

            // Act
            mockMvc.post("/v1/snippet/test/$testId") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.success") { value(true) }
                    jsonPath("$.message") { value("Test passed") }
                }
        }

        @Test
        fun `can update lint status`() {
            // Arrange
            val statusDto = StatusDto(snippetId = 1L, compliance = Compliance.COMPLIANT)

            `when`(snippetService.updateLintingCompliance(eq(statusDto))).thenAnswer { }

            // Act
            mockMvc.post("/v1/snippet/lint") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(statusDto)
            }
                .andExpect {
                    status { isOk() }
                }
        }

        @Test
        fun `can get formatted snippet`() {
            // Arrange
            val snippetId = 1L
            val formattedContent = "formatted snippet content"

            `when`(snippetService.formatSnippet(eq(snippetId))).thenReturn(formattedContent)

            // Act
            mockMvc.get("/v1/snippet/$snippetId/formatted") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { string(formattedContent) }
                }
        }
    }
