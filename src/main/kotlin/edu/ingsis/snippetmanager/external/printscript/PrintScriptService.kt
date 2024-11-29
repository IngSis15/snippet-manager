package edu.ingsis.snippetmanager.external.printscript

import edu.ingsis.snippetmanager.external.printscript.dto.ExecuteRequestDto
import edu.ingsis.snippetmanager.external.printscript.dto.ValidateResultDTO
import edu.ingsis.snippetmanager.snippet.dto.ExecuteResultDto
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Service
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.RestTemplate
import org.springframework.web.server.ResponseStatusException

@Service
class PrintScriptService(
    @Value("\${services.printscript.url}") val baseUrl: String,
) : PrintScriptApi {
    private val logger: Logger = LoggerFactory.getLogger(PrintScriptService::class.java)
    private lateinit var restTemplate: RestTemplate

    @PostConstruct
    fun init() {
        restTemplate = RestTemplate()
    }

    override fun validate(content: String): ValidateResultDTO? {
        val token = getToken() ?: throw IllegalStateException("Missing authentication token")

        val url = "$baseUrl/v1/validate"
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(token) }
            val request = HttpEntity(content, headers)
            val response: ResponseEntity<ValidateResultDTO> =
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ValidateResultDTO::class.java,
                )
            logger.info("Validation successful for content length: {}", content.length)
            response.body
        } catch (ex: HttpClientErrorException) {
            logger.error("Validation failed with status: {}, body: {}", ex.statusCode, ex.responseBodyAsString)
            throw ResponseStatusException(ex.statusCode, "Validation failed")
        } catch (ex: Exception) {
            logger.error("Unexpected error during validation: {}", ex.message, ex)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during validation")
        }
    }

    override fun execute(
        snippetId: Long,
        input: List<String>,
    ): ExecuteResultDto? {
        val token = getToken() ?: throw IllegalStateException("Missing authentication token")

        val url = "$baseUrl/v1/execute"
        val dto = ExecuteRequestDto("snippets", snippetId.toString(), input)
        return try {
            val headers = HttpHeaders().apply { setBearerAuth(token) }
            val request = HttpEntity(dto, headers)
            val response: ResponseEntity<ExecuteResultDto> =
                restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    ExecuteResultDto::class.java,
                )
            logger.info("Execution successful for snippetId: {}", snippetId)
            response.body
        } catch (ex: HttpClientErrorException) {
            logger.error(
                "Execution failed for snippetId: {}, status: {}, body: {}",
                snippetId,
                ex.statusCode,
                ex.responseBodyAsString,
            )
            throw ResponseStatusException(ex.statusCode, "Execution failed")
        } catch (ex: Exception) {
            logger.error("Unexpected error during execution for snippetId: {}", snippetId, ex)
            throw ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error during execution")
        }
    }

    private fun getToken(): String? {
        val authentication = SecurityContextHolder.getContext().authentication
        return if (authentication.principal is Jwt) {
            val jwt = authentication.principal as Jwt
            jwt.tokenValue
        } else {
            null
        }
    }
}
