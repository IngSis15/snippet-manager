package edu.ingsis.snippetmanager.server

import jakarta.servlet.Filter
import jakarta.servlet.FilterChain
import jakarta.servlet.ServletException
import jakarta.servlet.ServletRequest
import jakarta.servlet.ServletResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import java.io.IOException
import java.util.UUID

@Component
class CorrelationIdFilter : Filter {
    companion object {
        private const val CORRELATION_ID_KEY = "correlation-id"
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }

    @Throws(IOException::class, ServletException::class)
    override fun doFilter(
        request: ServletRequest,
        response: ServletResponse,
        chain: FilterChain,
    ) {
        if (request is HttpServletRequest && response is HttpServletResponse) {
            val correlationId = request.getHeader(CORRELATION_ID_HEADER) ?: UUID.randomUUID().toString()

            MDC.put(CORRELATION_ID_KEY, correlationId)
            response.setHeader(CORRELATION_ID_HEADER, correlationId)

            try {
                chain.doFilter(request, response)
            } finally {
                MDC.remove(CORRELATION_ID_KEY)
            }
        } else {
            chain.doFilter(request, response)
        }
    }
}
