package edu.ingsis.snippetmanager.server

import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import java.util.UUID

@Component
class CorrelationIdFilter : WebFilter {
    companion object {
        private const val CORRELATION_ID_KEY = "correlation-id"
        private const val CORRELATION_ID_HEADER = "X-Correlation-Id"
    }

    override fun filter(
        exchange: ServerWebExchange,
        chain: WebFilterChain,
    ): Mono<Void> {
        val correlationId: String = exchange.request.headers[CORRELATION_ID_HEADER]?.firstOrNull() ?: UUID.randomUUID().toString()
        MDC.put(CORRELATION_ID_KEY, correlationId)

        return chain.filter(exchange)
            .contextWrite { context -> context.put(CORRELATION_ID_KEY, correlationId) }
            .doFinally { MDC.remove(CORRELATION_ID_KEY) }
    }
}
