package edu.ingsis.snippetmanager.external.permission

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component

@Component
class PermissionApiConfiguration {
    @Bean
    @Profile("!test")
    fun permissionApi(
        @Value("localhos") baseUrl: String,
    ): PermissionApi {
        return PermissionService(baseUrl)
    }
}
