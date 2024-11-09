package edu.ingsis.snippetmanager.external

import edu.ingsis.snippetmanager.external.permission.PermissionApi
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.context.annotation.Profile

@Profile("test")
@TestConfiguration
class MockPermissionApiConfiguration{
    @Bean
    @Primary
    fun createMockPermissionApi(): PermissionApi {
        return PermissionMockApi()
    }

}
