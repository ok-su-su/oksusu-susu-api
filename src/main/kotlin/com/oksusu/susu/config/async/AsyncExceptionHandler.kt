package com.oksusu.susu.config.async

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler
import org.springframework.stereotype.Component
import java.lang.reflect.Method

@Component
class AsyncExceptionHandler : AsyncUncaughtExceptionHandler {
    val logger = KotlinLogging.logger { }

    override fun handleUncaughtException(throwable: Throwable, method: Method, vararg params: Any) {
        logger.error { "Exception message - $throwable" }
        logger.error { "Method name - " + method.name }
        for (param in params) {
            logger.error { "Parameter value - $param" }
        }
    }
}
