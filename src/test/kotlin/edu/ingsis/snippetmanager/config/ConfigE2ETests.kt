package edu.ingsis.snippetmanager.config

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.snippetmanager.config.configSchemas.FormattingSchemaDTO
import edu.ingsis.snippetmanager.config.configSchemas.LintingSchemaDTO
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.`when`
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.boot.test.mock.mockito.MockBean
import org.springframework.http.MediaType
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.jwt.JwtClaimNames
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.get
import org.springframework.test.web.servlet.put
import java.time.Instant

@WebMvcTest(ConfigRoutes::class)
class ConfigE2ETests
    @Autowired
    constructor(
        private val mockMvc: MockMvc,
        private val objectMapper: ObjectMapper,
    ) {
        @MockBean
        private lateinit var configService: ConfigService

        private lateinit var jwtToken: Jwt

        private val json = Json { ignoreUnknownKeys = true }

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
        fun `should get linting config for user`() {
            val lintingConfig = """
                {
                  "identifier_format": "camel case",
                  "mandatory-variable-or-literal-in-println": true,
                  "mandatory-variable-or-literal-in-readInput": true
                }
                """
            val returnable = json.decodeFromString<LintingSchemaDTO>(lintingConfig)

            `when`(configService.getLintingConfig("test-user")).thenReturn(returnable)

            mockMvc.get("/v1/config/linting") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.identifier_format") { value("camel case") }
                    jsonPath("$.mandatory-variable-or-literal-in-println") { value(true) }
                    jsonPath("$.mandatory-variable-or-literal-in-readInput") { value(true) }
                }
        }

        @Test
        fun `should get formatting config for user`() {
            val formattingConfig = """
                {
                  "enforce-spacing-before-colon-in-declaration": false,
                  "enforce-spacing-after-colon-in-declaration": false,
                  "enforce-no-spacing-around-equals": true,
                  "newLinesBeforePrintln": 0,
                  "indent-inside-if": 4
                }
                """

            val returnable = json.decodeFromString<FormattingSchemaDTO>(formattingConfig)

            `when`(configService.getFormattingConfig("test-user")).thenReturn(returnable)

            mockMvc.get("/v1/config/formatting") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.enforce-spacing-before-colon-in-declaration") { value(false) }
                    jsonPath("$.enforce-spacing-after-colon-in-declaration") { value(false) }
                    jsonPath("$.enforce-no-spacing-around-equals") { value(true) }
                    jsonPath("$.newLinesBeforePrintln") { value(0) }
                    jsonPath("$.indent-inside-if") { value(4) }
                }
        }

        @Test
        fun `should set linting config for user`() {
            val lintingConfig = """
                {
                  "identifier_format": "snake case",
                  "mandatory-variable-or-literal-in-println": true,
                  "mandatory-variable-or-literal-in-readInput": true
                }
                """

            val returnable = json.decodeFromString<LintingSchemaDTO>(lintingConfig)

            println(returnable)

            `when`(configService.setLintingConfig("test-user", returnable)).thenReturn(returnable)

            mockMvc.put("/v1/config/linting") {
                contentType = MediaType.APPLICATION_JSON
                content = lintingConfig
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.identifier_format") { value("snake case") }
                    jsonPath("$.mandatory-variable-or-literal-in-println") { value(true) }
                    jsonPath("$.mandatory-variable-or-literal-in-readInput") { value(true) }
                }
        }

        @Test
        fun `should set formatting config for user`() {
            val formattingConfig = """
        {
          "enforce-spacing-before-colon-in-declaration": false,
          "enforce-spacing-after-colon-in-declaration": true,
          "enforce-no-spacing-around-equals": false,
          "newLinesBeforePrintln": 2,
          "indent-inside-if": 2
        }
        """
            val returnable = json.decodeFromString<FormattingSchemaDTO>(formattingConfig)

            `when`(configService.setFormattingConfig("test-user", returnable)).thenReturn(returnable)

            mockMvc.put("/v1/config/formatting") {
                contentType = MediaType.APPLICATION_JSON
                content = formattingConfig
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.enforce-spacing-before-colon-in-declaration") { value(false) }
                    jsonPath("$.enforce-spacing-after-colon-in-declaration") { value(true) }
                    jsonPath("$.enforce-no-spacing-around-equals") { value(false) }
                    jsonPath("$.newLinesBeforePrintln") { value(2) }
                    jsonPath("$.indent-inside-if") { value(2) }
                }
        }
    }
