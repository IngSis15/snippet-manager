package edu.ingsis.snippetmanager.server

import org.slf4j.MDC
import org.springframework.http.HttpRequest
import org.springframework.http.client.ClientHttpRequestExecution
import org.springframework.http.client.ClientHttpRequestInterceptor
import org.springframework.http.client.ClientHttpResponse
import org.springframework.stereotype.Component
import java.util.UUID

@Component
class CorrelationIdInterceptor : ClientHttpRequestInterceptor {
    companion object {
        private const val CORRELATION_ID_KEY = "correlation-id"
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }

    override fun intercept(
        request: HttpRequest,
        body: ByteArray,
        execution: ClientHttpRequestExecution,
    ): ClientHttpResponse {
        val correlationId = MDC.get(CORRELATION_ID_KEY) ?: UUID.randomUUID().toString()
        request.headers.add(CORRELATION_ID_HEADER, correlationId)
        return execution.execute(request, body)
    }
}
