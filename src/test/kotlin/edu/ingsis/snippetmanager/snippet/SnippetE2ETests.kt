package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.MockPrintScriptApiConfiguration
import edu.ingsis.snippetmanager.snippet.dto.CreateSnippetDto
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // starts your spring server in a random port
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
@Import(MockPrintScriptApiConfiguration::class)
class SnippetE2ETests {
    @Autowired
    lateinit var client: WebTestClient

    @Autowired
    lateinit var repository: SnippetRepository

    @BeforeEach
    fun setup() {
        repository.saveAll(SnippetFixtures.all().map { translate(it) })
    }

    @AfterEach
    fun tearDown() {
        repository.deleteAll()
    }

    @Test
    fun `can get all snippets`() {
        client.get().uri(BASE)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.length()").isEqualTo(2)
    }

    @Test
    fun `can get snippet from id`() {
        val snippet = repository.findAll().first()
        client.get().uri("$BASE/${snippet.id}")
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content").isEqualTo(snippet.title)
    }

    @Test
    fun `can create snippet`() {
        val snippet =
            CreateSnippetDto(
                title = "Declaration",
                description = "This snippet declares a variable y",
                version = "1.1",
                content = "let y: number = 10;",
            )

        client.post().uri(BASE)
            .bodyValue(snippet)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.content").isEqualTo("let y: number = 10;")
    }

    @Test
    fun `can delete snippet`() {
        val snippet = repository.findAll().first()
        client.delete().uri("$BASE/${snippet.id}")
            .exchange()
            .expectStatus().isOk

        client.get().uri("$BASE/${snippet.id}")
            .exchange()
            .expectStatus().isNotFound
    }

    private fun translate(snippet: CreateSnippetDto): Snippet {
        return Snippet(
            title = snippet.title,
            description = snippet.description,
            version = snippet.version,
        )
    }

    companion object {
        private const val BASE = "/v1/snippet"
    }
}
