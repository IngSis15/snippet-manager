package edu.ingsis.snippetmanager.health

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.junit.jupiter.SpringExtension
import org.springframework.test.web.reactive.server.WebTestClient
import kotlin.test.Test

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ExtendWith(SpringExtension::class)
@AutoConfigureWebTestClient
class HealthE2ETest {
    @Autowired
    lateinit var client: WebTestClient

    @Test
    fun `test client can reach server`() {
        client.get()
            .uri("/health")
            .exchange().expectStatus().isOk
    }
}
