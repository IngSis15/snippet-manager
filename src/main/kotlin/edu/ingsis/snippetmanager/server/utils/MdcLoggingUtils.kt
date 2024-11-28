package edu.ingsis.snippetmanager.server.utils

import org.slf4j.Logger
import org.slf4j.MDC
import reactor.core.publisher.Signal

object MdcLoggingUtils {
    fun <T> logWithMdc(
        signal: Signal<T>,
        status: LogTypes,
        logger: Logger,
        message: String,
        correlationId: String,
    ) {
        MDC.put("correlation-id", correlationId)
        try {
            when (status) {
                LogTypes.INFO -> logger.info(message)
                LogTypes.DEBUG -> logger.debug(message)
                LogTypes.ERROR -> logger.error(message)
                LogTypes.WARN -> logger.warn(message)
            }
        } finally {
            MDC.remove("correlation-id")
        }
    }
}
