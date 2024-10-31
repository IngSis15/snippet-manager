package edu.ingsis.snippetmanager.config

import com.fasterxml.jackson.databind.ObjectMapper
import edu.ingsis.snippetmanager.config.dto.FormattingConfigDto
import edu.ingsis.snippetmanager.config.dto.LintingConfigDto
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
            val lintingConfig = LintingConfigDto(camelCase = true, expressionAllowedInPrint = true, expressionAllowedInReadInput = false)
            `when`(configService.getLintingConfig("test-user")).thenReturn(lintingConfig)

            mockMvc.get("/v1/config/linting") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.camelCase") { value(true) }
                    jsonPath("$.expressionAllowedInPrint") { value(true) }
                    jsonPath("$.expressionAllowedInReadInput") { value(false) }
                }
        }

        @Test
        fun `should get formatting config for user`() {
            val formattingConfig =
                FormattingConfigDto(
                    spaceBeforeColon = true,
                    spaceAfterColon = false,
                    spaceAroundAssignment = true,
                    newLinesBeforePrintln = 1,
                    indentSpaces = 4,
                )
            `when`(configService.getFormattingConfig("test-user")).thenReturn(formattingConfig)

            mockMvc.get("/v1/config/formatting") {
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    content { contentType(MediaType.APPLICATION_JSON) }
                    jsonPath("$.spaceBeforeColon") { value(true) }
                    jsonPath("$.spaceAfterColon") { value(false) }
                    jsonPath("$.spaceAroundAssignment") { value(true) }
                    jsonPath("$.newLinesBeforePrintln") { value(1) }
                    jsonPath("$.indentSpaces") { value(4) }
                }
        }

        @Test
        fun `should set linting config for user`() {
            val lintingConfig = LintingConfigDto(camelCase = false, expressionAllowedInPrint = true, expressionAllowedInReadInput = true)
            `when`(configService.setLintingConfig("test-user", lintingConfig)).thenReturn(lintingConfig)

            mockMvc.put("/v1/config/linting") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(lintingConfig)
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.camelCase") { value(false) }
                    jsonPath("$.expressionAllowedInPrint") { value(true) }
                    jsonPath("$.expressionAllowedInReadInput") { value(true) }
                }
        }

        @Test
        fun `should set formatting config for user`() {
            val formattingConfig =
                FormattingConfigDto(
                    spaceBeforeColon = false,
                    spaceAfterColon = true,
                    spaceAroundAssignment = false,
                    newLinesBeforePrintln = 2,
                    indentSpaces = 2,
                )
            `when`(configService.setFormattingConfig("test-user", formattingConfig)).thenReturn(formattingConfig)

            mockMvc.put("/v1/config/formatting") {
                contentType = MediaType.APPLICATION_JSON
                content = objectMapper.writeValueAsString(formattingConfig)
                with(jwt().jwt(jwtToken))
            }
                .andExpect {
                    status { isOk() }
                    jsonPath("$.spaceBeforeColon") { value(false) }
                    jsonPath("$.spaceAfterColon") { value(true) }
                    jsonPath("$.spaceAroundAssignment") { value(false) }
                    jsonPath("$.newLinesBeforePrintln") { value(2) }
                    jsonPath("$.indentSpaces") { value(2) }
                }
        }
    }
