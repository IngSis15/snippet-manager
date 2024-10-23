package edu.ingsis.snippetmanager.external

import edu.ingsis.snippetmanager.external.asset.AssetApi
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("test")
@TestConfiguration
class MockAssetApiConfiguration {
    @Bean
    @Primary
    fun createMockAssetApi(): AssetApi {
        return AssetMockApi()
    }
}
