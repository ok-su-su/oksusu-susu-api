package com.oksusu.susu.common.extension

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.withContext
import org.slf4j.MDC
import kotlin.coroutines.CoroutineContext

suspend fun <T> withMDCContext(
    context: CoroutineContext = Dispatchers.IO,
    block: suspend () -> T,
): T {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    return withContext(context + MDCContext(contextMap)) { block() }
}

suspend fun <T> withJob(
    context: CoroutineContext = Dispatchers.IO,
    job: Job = Job(),
    block: suspend () -> T,
): T {
    return withContext(context + job) { block() }
}

fun mdcCoroutineScope(context: CoroutineContext, traceId: String): CoroutineScope {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    contextMap.plus("traceId" to traceId)
    return CoroutineScope(context + MDCContext(contextMap))
}
