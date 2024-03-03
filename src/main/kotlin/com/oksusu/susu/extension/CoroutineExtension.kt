package com.oksusu.susu.extension

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.slf4j.MDCContext
import kotlinx.coroutines.slf4j.MDCContextMap
import org.slf4j.MDC
import kotlin.coroutines.CoroutineContext

fun CoroutineDispatcher.withMDCContext(contextMap: MDCContextMap = MDC.getCopyOfContextMap()): CoroutineContext {
    return this + MDCContext(contextMap)
}

fun CoroutineDispatcher.withJob(): CoroutineContext {
    return this + Job()
}
