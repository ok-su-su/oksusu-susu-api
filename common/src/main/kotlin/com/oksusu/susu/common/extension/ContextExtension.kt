package com.oksusu.susu.common.extension

import org.slf4j.MDC
import reactor.util.context.Context

/**
 * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
 * State of the MDC after calling this method should be same as Reactor [Context] state.
 * One thread-local access only.
 */
fun Context.copyToMdc() {
    if (!this.isEmpty) {
        val map = this.toMap()
        MDC.setContextMap(map)
    } else {
        MDC.clear()
    }
}

private fun Context.toMap(): Map<String, String> = this.stream()
    .map { ctx -> ctx.key.toString() to ctx.value.toString() }
    .toList().toMap()
