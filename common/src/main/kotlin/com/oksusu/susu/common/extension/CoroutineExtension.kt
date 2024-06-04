package com.oksusu.susu.common.extension

import arrow.fx.coroutines.parZip
import com.oksusu.susu.common.consts.MDC_KEY_TRACE_ID
import com.oksusu.susu.common.coroutine.MdcContinuationInterceptor
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
    return withContext(context + MdcContinuationInterceptor()) { block() }
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
    contextMap.plus(MDC_KEY_TRACE_ID to traceId)
    return CoroutineScope(context + MDCContext(contextMap) + MdcContinuationInterceptor())
}

suspend inline fun <A, B, C> parZipWithMDC(
    crossinline fa: suspend CoroutineScope.() -> A,
    crossinline fb: suspend CoroutineScope.() -> B,
    crossinline f: suspend CoroutineScope.(A, B) -> C,
): C {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    val ctx = Dispatchers.IO + MDCContext(contextMap) + MdcContinuationInterceptor()
    return parZip(ctx, fa, fb, f)
}

suspend inline fun <A, B, C, D> parZipWithMDC(
    crossinline fa: suspend CoroutineScope.() -> A,
    crossinline fb: suspend CoroutineScope.() -> B,
    crossinline fc: suspend CoroutineScope.() -> C,
    crossinline f: suspend CoroutineScope.(A, B, C) -> D,
): D {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    val ctx = Dispatchers.IO + MDCContext(contextMap) + MdcContinuationInterceptor()
    return parZip(ctx, fa, fb, fc, f)
}

suspend inline fun <A, B, C, D, E> parZipWithMDC(
    crossinline fa: suspend CoroutineScope.() -> A,
    crossinline fb: suspend CoroutineScope.() -> B,
    crossinline fc: suspend CoroutineScope.() -> C,
    crossinline fd: suspend CoroutineScope.() -> D,
    crossinline f: suspend CoroutineScope.(A, B, C, D) -> E,
): E {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    val ctx = Dispatchers.IO + MDCContext(contextMap) + MdcContinuationInterceptor()
    return parZip(ctx, fa, fb, fc, fd, f)
}

suspend inline fun <A, B, C, D, E, F> parZipWithMDC(
    crossinline fa: suspend CoroutineScope.() -> A,
    crossinline fb: suspend CoroutineScope.() -> B,
    crossinline fc: suspend CoroutineScope.() -> C,
    crossinline fd: suspend CoroutineScope.() -> D,
    crossinline fe: suspend CoroutineScope.() -> E,
    crossinline f: suspend CoroutineScope.(A, B, C, D, E) -> F,
): F {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    val ctx = Dispatchers.IO + MDCContext(contextMap) + MdcContinuationInterceptor()
    return parZip(ctx, fa, fb, fc, fd, fe, f)
}

suspend inline fun <A, B, C, D, E, F, G> parZipWithMDC(
    crossinline fa: suspend CoroutineScope.() -> A,
    crossinline fb: suspend CoroutineScope.() -> B,
    crossinline fc: suspend CoroutineScope.() -> C,
    crossinline fd: suspend CoroutineScope.() -> D,
    crossinline fe: suspend CoroutineScope.() -> E,
    crossinline ff: suspend CoroutineScope.() -> F,
    crossinline f: suspend CoroutineScope.(A, B, C, D, E, F) -> G,
): G {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    val ctx = Dispatchers.IO + MDCContext(contextMap) + MdcContinuationInterceptor()
    return parZip(ctx, fa, fb, fc, fd, fe, ff, f)
}

suspend inline fun <A, B, C, D, E, F, G, H> parZipWithMDC(
    crossinline fa: suspend CoroutineScope.() -> A,
    crossinline fb: suspend CoroutineScope.() -> B,
    crossinline fc: suspend CoroutineScope.() -> C,
    crossinline fd: suspend CoroutineScope.() -> D,
    crossinline fe: suspend CoroutineScope.() -> E,
    crossinline ff: suspend CoroutineScope.() -> F,
    crossinline fg: suspend CoroutineScope.() -> G,
    crossinline f: suspend CoroutineScope.(A, B, C, D, E, F, G) -> H,
): H {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    val ctx = Dispatchers.IO + MDCContext(contextMap) + MdcContinuationInterceptor()
    return parZip(ctx, fa, fb, fc, fd, fe, ff, fg, f)
}

suspend inline fun <A, B, C, D, E, F, G, H, I> parZipWithMDC(
    crossinline fa: suspend CoroutineScope.() -> A,
    crossinline fb: suspend CoroutineScope.() -> B,
    crossinline fc: suspend CoroutineScope.() -> C,
    crossinline fd: suspend CoroutineScope.() -> D,
    crossinline fe: suspend CoroutineScope.() -> E,
    crossinline ff: suspend CoroutineScope.() -> F,
    crossinline fg: suspend CoroutineScope.() -> G,
    crossinline fh: suspend CoroutineScope.() -> H,
    crossinline f: suspend CoroutineScope.(A, B, C, D, E, F, G, H) -> I,
): I {
    val contextMap = MDC.getCopyOfContextMap() ?: emptyMap()
    val ctx = Dispatchers.IO + MDCContext(contextMap) + MdcContinuationInterceptor()
    return parZip(ctx, fa, fb, fc, fd, fe, ff, fg, fh, f)
}
