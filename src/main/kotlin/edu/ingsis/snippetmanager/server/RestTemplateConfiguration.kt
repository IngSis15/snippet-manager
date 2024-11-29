package edu.ingsis.snippetmanager.server

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfiguration(private val correlationIdInterceptor: CorrelationIdInterceptor) {
    @Bean
    fun restTemplate(): RestTemplate {
        val restTemplate = RestTemplate()
        restTemplate.interceptors.add(correlationIdInterceptor)
        return restTemplate
    }
}
