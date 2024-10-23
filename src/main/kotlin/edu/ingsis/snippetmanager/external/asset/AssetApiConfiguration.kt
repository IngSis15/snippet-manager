package edu.ingsis.snippetmanager.external.asset

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class AssetApiConfiguration {
    @Bean
    @Profile("!test")
    fun assetApi(
        @Value("\${services.asset.url}") baseUrl: String,
    ): AssetApi {
        return AssetService(baseUrl)
    }
}
