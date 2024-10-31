package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.Snippet
import edu.ingsis.snippetmanager.snippet.SnippetRepository
import edu.ingsis.snippetmanager.test.dto.CreateTestDTO
import edu.ingsis.snippetmanager.test.dto.UpdateTestDTO
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@ActiveProfiles("test")
class TestControllerE2ETests {
    @Autowired
    lateinit var client: WebTestClient

    @Autowired
    lateinit var testRepository: TestRepository

    @Autowired
    lateinit var snippetRepository: SnippetRepository

    @BeforeEach
    fun setup() {
        val snippet =
            Snippet(
                name = "Test Snippet",
                description = "A snippet for testing",
                language = "printscript",
                version = "1.1",
                extension = "ps",
            )

        val savedSnippet = snippetRepository.save(snippet)

        val test =
            Test(
                snippet = savedSnippet,
                expectedOutput = "Expected Output",
                userInput = "User Input",
                environmentVariables = mapOf("ENV_VAR" to "value"),
            )

        val savedTest = testRepository.save(test)

        savedTest.environmentVariables
    }

    @AfterEach
    fun tearDown() {
        testRepository.deleteAll()
    }

    @Test
    fun `should create Test`() {
        val snippetId = snippetRepository.findAll().first().id

        val dto =
            snippetId?.let {
                CreateTestDTO(
                    snippetId = it,
                    expectedOutput = "Expected Output",
                    userInput = "User Input",
                    environmentVariables = mapOf("ENV_VAR" to "value"),
                )
            }

        if (dto != null) {
            client.post().uri(BASE)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated
                .expectBody()
                .jsonPath("$.snippet.id").isEqualTo(snippetId)
                .jsonPath("$.expectedOutput").isEqualTo(dto.expectedOutput)
        }
    }

    @Test
    fun `should update Test`() {
        val test = testRepository.findAll().first()
        val dto =
            UpdateTestDTO(
                expectedOutput = "Updated Expected Output",
                userInput = "Updated User Input",
                environmentVariables = mapOf("NEW_ENV_VAR" to "new_value"),
            )

        client.put().uri("$BASE/${test.id}")
            .bodyValue(dto)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.expectedOutput").isEqualTo("Updated Expected Output")
            .jsonPath("$.userInput").isEqualTo("Updated User Input")
    }

    @Test
    fun `should delete Test`() {
        val test = testRepository.findAll().first()

        client.delete().uri("$BASE/${test.id}")
            .exchange()
            .expectStatus().isNoContent

        client.get().uri("$BASE/${test.id}")
            .exchange()
            .expectStatus().isNotFound
    }

    companion object {
        private const val BASE = "/v1/tests"
    }
}
