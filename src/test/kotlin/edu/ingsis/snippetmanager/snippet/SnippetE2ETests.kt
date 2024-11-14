package edu.ingsis.snippetmanager.snippet

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
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
        fun `can get all snippets`() {
            val snippets =
                SnippetFixtures.all().map { snippet ->
                    SnippetDto(
                        id = snippet.id,
                        name = snippet.name,
                        description = snippet.description,
                        language = snippet.language,
                        compliance = snippet.compliance.toString(),
                        extension = snippet.extension,
                        content = SnippetFixtures.getContentFromName(snippet.name),
                        permission = "VIEWER",
                    )
                }
            mockMvc.post("/v1/snippet") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(snippets[0])
                with(jwt().jwt(jwtToken))
            }.andExpect { status { isCreated() } }

            mockMvc.get("/v1/snippet") {
                param("page", "0")
                param("size", "10")
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                }
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
    }
