package edu.ingsis.snippetmanager.snippet

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import edu.ingsis.snippetmanager.snippet.dto.SnippetDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.doNothing
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.oauth2.resource.OAuth2ResourceServerProperties
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.delete
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.post
import java.time.Instant

@WebMvcTest(SnippetRoutes::class)
class SnippetE2ETests @Autowired constructor(private val mockMvc: MockMvc, private val objectMapper: ObjectMapper) {

    @MockBean
    private lateinit var snippetService: SnippetService

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
        val snippet = SnippetFixtures.all().first()
        val snippetContent = "let y: number = 10;"

        `when`(snippetService.getSnippet(snippet.id ?: throw IllegalArgumentException("Snippet ID is null"), jwtToken)).thenReturn(
            SnippetDto(
                id = snippet.id,
                name = snippet.name,
                description = snippet.description,
                content = snippetContent,
                language = snippet.language,
                version = snippet.version,
                extension = snippet.extension,
                permission = "VIEWER"
            )
        )

        mockMvc.get("/v1/snippet/${snippet.id}") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content") { value(snippetContent) }
                jsonPath("$.name") { value(snippet.name) }
                jsonPath("$.description") { value(snippet.description) }
            }
    }

    @Test
    fun `can get snippet from id`() {
        val snippet = SnippetFixtures.all().first()
        val snippetContent = "let y: number = 10;"

        `when`(snippetService.getSnippet(snippet.id ?: throw IllegalArgumentException("Snippet ID is null"), jwtToken)).thenReturn(
            SnippetDto(
                id = snippet.id,
                name = snippet.name,
                description = snippet.description,
                content = snippetContent,
                language = snippet.language,
                version = snippet.version,
                extension = snippet.extension,
                permission = "VIEWER"
            )
        )

        mockMvc.get("/v1/snippet/${snippet.id}") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content") { value(snippetContent) }
                jsonPath("$.name") { value(snippet.name) }
                jsonPath("$.description") { value(snippet.description) }
            }
    }

    @Test
    fun `can create snippet`() {
        val snippet = CreateSnippetDto(
            name = "Declaration",
            description = "This snippet declares a variable y",
            language = "printscript",
            version = "1.1",
            content = "let y: number = 10;",
            extension = "ps"
        )

        `when`(snippetService.createSnippet(snippet, jwtToken)).thenReturn(
            SnippetDto(
                id = 1L,
                name = snippet.name,
                description = snippet.description,
                content = snippet.content,
                language = snippet.language,
                version = snippet.version,
                extension = snippet.extension,
                permission = "OWNER"
            )
        )

        mockMvc.post("/v1/snippet") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(snippet)
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isCreated() }
                jsonPath("$.content") { value("let y: number = 10;") }
            }
    }

    @Test
    fun `can delete snippet`() {
        val snippet = SnippetFixtures.all().first()
        val snippetId = snippet.id ?: throw IllegalArgumentException("Snippet ID is null")

        // Mock the deleteSnippet method to simulate deletion
        doNothing().`when`(snippetService).deleteSnippet(snippetId, jwtToken)

        mockMvc.delete("/v1/snippet/$snippetId") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
            }

        // Mock the getSnippet method to throw a NoSuchElementException after deletion
        `when`(snippetService.getSnippet(snippetId, jwtToken)).thenThrow(NoSuchElementException::class.java)

        mockMvc.get("/v1/snippet/$snippetId") {
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isNotFound() }
            }
    }

    @Test
    fun `can edit snippet`() {
        val snippet = SnippetFixtures.all().first()
        val editedSnippet = CreateSnippetDto(
            name = snippet.name,
            description = snippet.description,
            language = snippet.language,
            version = snippet.version,
            extension = snippet.extension,
            content = "let a: number = 1;"
        )

        `when`(snippetService.editSnippet(editedSnippet, snippet.id ?: throw IllegalArgumentException("Snippet ID is null"), jwtToken)).thenReturn(
            SnippetDto(
                id = snippet.id,
                name = snippet.name,
                description = snippet.description,
                content = editedSnippet.content,
                language = snippet.language,
                version = snippet.version,
                extension = snippet.extension,
                permission = "OWNER"
            )
        )
        .thenReturn(
            SnippetDto(
                id = snippet.id,
                name = snippet.name,
                description = snippet.description,
                content = editedSnippet.content,
                language = snippet.language,
                version = snippet.version,
                extension = snippet.extension,
                permission = "OWNER"
            )
        )

        mockMvc.post("/v1/snippet/${snippet.id}") {
            contentType = MediaType.APPLICATION_JSON
            content = objectMapper.writeValueAsString(editedSnippet)
            with(jwt().jwt(jwtToken))
        }
            .andExpect {
                status { isOk() }
                jsonPath("$.content") { value("let a: number = 1;") }
            }
    }

}
