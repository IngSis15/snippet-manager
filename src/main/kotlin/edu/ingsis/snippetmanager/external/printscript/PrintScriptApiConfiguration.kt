package edu.ingsis.snippetmanager.external.printscript

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class PrintScriptApiConfiguration {
    @Bean
    @Profile("!test")
    fun printScriptApi(
        @Value("\${services.printscript.url}") baseUrl: String,
        restTemplate: RestTemplate,
    ): PrintScriptApi {
        return PrintScriptService(baseUrl, restTemplate)
    }
}
