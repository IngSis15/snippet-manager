package edu.ingsis.snippetmanager.snippet

import edu.ingsis.snippetmanager.external.printscript.PrintScriptApi
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("test")
@TestConfiguration
class MockPrintScriptApiConfiguration {
    @Bean
    @Primary
    fun createMockPrintScriptApi(): PrintScriptApi {
        println("hello")
        return PrintScriptApiMock()
    }
}
