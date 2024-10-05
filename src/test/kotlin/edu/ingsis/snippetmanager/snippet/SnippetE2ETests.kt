package edu.ingsis.snippetmanager.snippet

import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT) // starts your spring server in a random port
@ExtendWith(SpringExtension::class)
@ActiveProfiles(value = ["test"])
@AutoConfigureWebTestClient // sets up an adecuate 'WebTestClient'
class SnippetE2ETests
    @Autowired
    constructor(
        val client: WebTestClient,
        val repository: SnippetRepository,
    ) {
        @BeforeEach
        fun setup() {
            repository.saveAll(SnippetFixtures.all())
        }

        @AfterEach
        fun tearDown() {
            repository.deleteAll()
        }

        @Test
        fun `can get snippet from id`() {
            client.get().uri("$BASE/1")
                .exchange()
                .expectStatus().isOk
                .expectBody()
                .jsonPath("$.content").isEqualTo("let x: number = 5;")
        }

        companion object {
            private const val BASE = "/v1/snippet"
        }
    }
