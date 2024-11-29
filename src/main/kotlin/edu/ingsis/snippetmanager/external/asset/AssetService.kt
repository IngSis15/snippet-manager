package edu.ingsis.snippetmanager.external.asset

import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Service
class AssetService(
    @Value("\${services.asset.url}") val baseUrl: String,
) : AssetApi {
    private val logger: Logger = LoggerFactory.getLogger(AssetService::class.java)
    private lateinit var restTemplate: RestTemplate

    @PostConstruct
    fun init() {
        restTemplate = RestTemplate()
    }

    override fun getAsset(
        container: String,
        key: String,
    ): String? {
        val url = "$baseUrl/v1/asset/$container/$key"
        return try {
            val response: ResponseEntity<String> = restTemplate.getForEntity(url, String::class.java)
            logger.info("Successfully fetched asset: container={}, key={}", container, key)
            response.body
        } catch (ex: HttpClientErrorException) {
            logger.error("Error fetching asset: container={}, key={}, status={}", container, key, ex.statusCode)
            throw ResponseStatusException(ex.statusCode, "Error while fetching asset")
        } catch (ex: Exception) {
            logger.error("Unexpected error while fetching asset: container={}, key={}, error={}", container, key, ex.message)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error")
        }
    }

    override fun createAsset(
        container: String,
        key: String,
        content: String,
    ) {
        val url = "$baseUrl/v1/asset/$container/$key"
        try {
            val request = HttpEntity(content)
            restTemplate.exchange(url, HttpMethod.PUT, request, Void::class.java)
            logger.info("Successfully created asset: container={}, key={}", container, key)
        } catch (ex: HttpClientErrorException) {
            logger.error("Error creating asset: container={}, key={}, status={}", container, key, ex.statusCode)
            throw ResponseStatusException(ex.statusCode, "Error while creating asset")
        } catch (ex: Exception) {
            logger.error("Unexpected error while creating asset: container={}, key={}, error={}", container, key, ex.message)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error")
        }
    }

    override fun deleteAsset(
        container: String,
        key: String,
    ) {
        val url = "$baseUrl/v1/asset/$container/$key"
        try {
            restTemplate.delete(url)
            logger.info("Successfully deleted asset: container={}, key={}", container, key)
        } catch (ex: HttpClientErrorException) {
            logger.error("Error deleting asset: container={}, key={}, status={}", container, key, ex.statusCode)
            throw ResponseStatusException(ex.statusCode, "Error while deleting asset")
        } catch (ex: Exception) {
            logger.error("Unexpected error while deleting asset: container={}, key={}, error={}", container, key, ex.message)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error")
        }
    }
}
