package edu.ingsis.snippetmanager.external.asset

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class AssetApiConfiguration {
    @Bean
    @Profile("!test")
    fun assetApi(
        @Value("\${services.asset.url}") baseUrl: String,
        restTemplate: RestTemplate,
    ): AssetApi {
        return AssetService(baseUrl, restTemplate)
    }
}
