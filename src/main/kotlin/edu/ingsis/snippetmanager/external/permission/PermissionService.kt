package edu.ingsis.snippetmanager.external.permission

import edu.ingsis.snippetmanager.external.permission.dto.PermissionResponseDTO
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Service
class PermissionService(
    @Value("\${services.permission.url}") val baseUrl: String,
) : PermissionApi {
    private val logger: Logger = LoggerFactory.getLogger(PermissionService::class.java)
    private lateinit var restTemplate: RestTemplate

    @PostConstruct
    fun init() {
        restTemplate = RestTemplate()
    }

    override fun getAllSnippetPermissions(jwt: Jwt): List<PermissionResponseDTO> {
        val url = "$baseUrl/permissions/user"
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(jwt.tokenValue) }
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    Array<PermissionResponseDTO>::class.java,
                )
            logger.info("Completed fetching all snippet permissions")
            response.body?.toList() ?: emptyList()
        } catch (ex: HttpClientErrorException) {
            logger.error("Error fetching all snippet permissions: {}", ex.message)
            throw ResponseStatusException(ex.statusCode, "Error fetching permissions")
        }
    }

    override fun getAllOwnerSnippetPermissions(jwt: Jwt): List<PermissionResponseDTO> {
        val url = "$baseUrl/permissions/permissionType?permissionType=OWNER"
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(jwt.tokenValue) }
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    Array<PermissionResponseDTO>::class.java,
                )
            logger.info("Completed fetching owner snippet permissions")
            response.body?.toList() ?: emptyList()
        } catch (ex: HttpClientErrorException) {
            logger.error("Error fetching owner snippet permissions: {}", ex.message)
            throw ResponseStatusException(ex.statusCode, "Error fetching owner permissions")
        }
    }

    override fun canRead(
        jwt: Jwt,
        snippetId: Long,
    ): Boolean {
        val url = "$baseUrl/permissions/user/snippet/$snippetId"
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(jwt.tokenValue) }
            restTemplate.exchange(
                url,
                HttpMethod.GET,
                HttpEntity<Any>(headers),
                PermissionResponseDTO::class.java,
            )
            logger.info("Read permission granted for snippetId={}", snippetId)
            true
        } catch (ex: HttpClientErrorException.NotFound) {
            logger.warn("Read permission denied for snippetId={}: Not Found", snippetId)
            false
        } catch (ex: Exception) {
            logger.error("Error checking read permission for snippetId={}: {}", snippetId, ex.message)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking read permission")
        }
    }

    override fun canModify(
        jwt: Jwt,
        snippetId: Long,
    ): Boolean {
        val url = "$baseUrl/permissions/user/snippet/$snippetId"
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(jwt.tokenValue) }
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    PermissionResponseDTO::class.java,
                )
            val canModify = response.body?.permissionType == "OWNER"
            logger.info("Modify permission result for snippetId={}: {}", snippetId, canModify)
            canModify
        } catch (ex: HttpClientErrorException.NotFound) {
            logger.warn("Modify permission denied for snippetId={}: Not Found", snippetId)
            false
        } catch (ex: Exception) {
            logger.error("Error checking modify permission for snippetId={}: {}", snippetId, ex.message)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error checking modify permission")
        }
    }

    override fun getPermission(
        jwt: Jwt,
        snippetId: Long,
    ): PermissionResponseDTO? {
        val url = "$baseUrl/permissions/user/snippet/$snippetId"
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(jwt.tokenValue) }
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    HttpEntity<Any>(headers),
                    PermissionResponseDTO::class.java,
                )
            logger.info("Fetched permission for snippetId={}: {}", snippetId, response.body)
            response.body
        } catch (ex: HttpClientErrorException) {
            logger.error("Error fetching permission for snippetId={}: {}", snippetId, ex.message)
            throw ResponseStatusException(ex.statusCode, "Error fetching permission")
        }
    }

    override fun addPermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): PermissionResponseDTO? {
        val url = "$baseUrl/permissions/assign"
        val requestBody = mapOf("snippetId" to snippetId, "permissionType" to permission)
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(jwt.tokenValue) }
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    HttpEntity(requestBody, headers),
                    PermissionResponseDTO::class.java,
                )
            logger.info("Added permission for snippetId={}: {}", snippetId, response.body)
            response.body ?: throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to add permission")
        } catch (ex: HttpClientErrorException) {
            logger.error("Error adding permission for snippetId={}: {}", snippetId, ex.message)
            throw ResponseStatusException(ex.statusCode, "Error adding permission")
        }
    }

    override fun removePermission(
        jwt: Jwt,
        snippetId: Long,
        permission: String,
    ): PermissionResponseDTO? {
        val url = "$baseUrl/permissions/user/snippet/$snippetId?permissionType=$permission"
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(jwt.tokenValue) }
            val response =
                restTemplate.exchange(
                    url,
                    HttpMethod.DELETE,
                    HttpEntity<Any>(headers),
                    PermissionResponseDTO::class.java,
                )
            logger.info("Removed permission for snippetId={}: {}", snippetId, response.body)
            response.body
        } catch (ex: HttpClientErrorException) {
            logger.error("Error removing permission for snippetId={}: {}", snippetId, ex.message)
            throw ResponseStatusException(ex.statusCode, "Error removing permission")
        }
    }
}
