package edu.ingsis.snippetmanager.test

import edu.ingsis.snippetmanager.snippet.Compliance
import edu.ingsis.snippetmanager.snippet.Snippet
import edu.ingsis.snippetmanager.snippet.SnippetRepository
import edu.ingsis.snippetmanager.test.dto.CreateTestDTO
import edu.ingsis.snippetmanager.test.dto.UpdateTestDTO
import jakarta.transaction.Transactional
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.PageRequest
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
                compliance = Compliance.PENDING,
                extension = "ps",
            )

        val savedSnippet = snippetRepository.save(snippet)

        val test =
            Test(
                name = "Test Name",
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

    @org.junit.jupiter.api.Test
    fun `should create Test`() {
        val pageable = PageRequest.of(0, 10)
        val snippetId = snippetRepository.findAll(pageable).content.first().id!!

        val dto =
            CreateTestDTO(
                snippetId = snippetId,
                name = "Test Name",
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

    @org.junit.jupiter.api.Test
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

    @org.junit.jupiter.api.Test
    fun `should delete Test`() {
        val test = testRepository.findAll().first()

        client.delete().uri("$BASE/${test.id}")
            .exchange()
            .expectStatus().isNoContent

        client.get().uri("$BASE/${test.id}")
            .exchange()
            .expectStatus().isNotFound
    }

    @Transactional
    @org.junit.jupiter.api.Test
    fun `should get all Tests by id`() {
        val test = testRepository.findAll()

        client.get().uri("$BASE/snippet/${test.first().snippet.id}")
            .exchange()
            .expectStatus().isOk
    }

    companion object {
        private const val BASE = "/v1/tests"
    }
}
