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
                expectedOutput = listOf("Expected Output Line 1", "Expected Output Line 2"),
                userInput = listOf("User Input Line 1", "User Input Line 2"),
            )

        testRepository.save(test)
    }

    @AfterEach
    fun tearDown() {
        testRepository.deleteAll()
        snippetRepository.deleteAll()
    }

    @Test
    fun `should create Test`() {
        val snippetId = snippetRepository.findAll().first().id!!

        val dto =
            CreateTestDTO(
                snippetId = snippetId,
                expectedOutput = listOf("Expected Output Line 1", "Expected Output Line 2"),
                userInput = listOf("User Input Line 1", "User Input Line 2"),
            )

        client.post().uri(BASE)
            .bodyValue(dto)
            .exchange()
            .expectStatus().isCreated
            .expectBody()
            .jsonPath("$.snippet.id").isEqualTo(snippetId)
            .jsonPath("$.expectedOutput[0]").isEqualTo(dto.expectedOutput[0])
            .jsonPath("$.expectedOutput[1]").isEqualTo(dto.expectedOutput[1])
            .jsonPath("$.userInput[0]").isEqualTo(dto.userInput[0])
            .jsonPath("$.userInput[1]").isEqualTo(dto.userInput[1])
    }

    @Test
    fun `should update Test`() {
        val test = testRepository.findAll().first()
        val dto =
            UpdateTestDTO(
                expectedOutput = listOf("Updated Expected Output Line 1", "Updated Expected Output Line 2"),
                userInput = listOf("Updated User Input Line 1", "Updated User Input Line 2"),
            )

        client.put().uri("$BASE/${test.id}")
            .bodyValue(dto)
            .exchange()
            .expectStatus().isOk
            .expectBody()
            .jsonPath("$.expectedOutput[0]").isEqualTo("Updated Expected Output Line 1")
            .jsonPath("$.expectedOutput[1]").isEqualTo("Updated Expected Output Line 2")
            .jsonPath("$.userInput[0]").isEqualTo("Updated User Input Line 1")
            .jsonPath("$.userInput[1]").isEqualTo("Updated User Input Line 2")
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
