package edu.ingsis.snippetmanager.config

import edu.ingsis.snippetmanager.config.dto.FormattingConfigDto
import edu.ingsis.snippetmanager.config.dto.LintingConfigDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@ActiveProfiles("test")
class ConfigServiceTests {

    @Autowired
    lateinit var configService: ConfigService

    @Test
    fun `test get linting config`() {
        val userId = "testUserId"
        val lintingConfig = configService.getLintingConfig(userId)
        assertNotNull(lintingConfig)
        // Add more assertions based on the expected response
    }

    @Test
    fun `test set linting config`() {
        val userId = "testUserId"
        val lintingConfig = LintingConfigDto(camelCase = true, expressionAllowedInPrint = true, expressionAllowedInReadInput = false)
        configService.setLintingConfig(userId, lintingConfig)
        val updatedConfig = configService.getLintingConfig(userId)
        assertEquals(lintingConfig, updatedConfig)
    }

    @Test
    fun `test get formatting config`() {
        val userId = "testUserId"
        val formattingConfig = configService.getFormattingConfig(userId)
        assertNotNull(formattingConfig)
        // Add more assertions based on the expected response
    }

    @Test
    fun `test set formatting config`() {
        val userId = "testUserId"
        val formattingConfig = FormattingConfigDto(spaceBeforeColon = false, spaceAfterColon = false, spaceAroundAssignment = true, newLinesBeforePrintln = 0, indentSpaces = 4)
        configService.setFormattingConfig(userId, formattingConfig)
        val updatedConfig = configService.getFormattingConfig(userId)
        assertEquals(formattingConfig, updatedConfig)
    }
}