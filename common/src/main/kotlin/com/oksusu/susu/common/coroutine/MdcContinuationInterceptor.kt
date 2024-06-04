package com.oksusu.susu.common.coroutine

import com.oksusu.susu.common.consts.MDC_KEY_TRACE_ID
import kotlinx.coroutines.reactor.ReactorContext
import org.slf4j.MDC
import kotlin.coroutines.Continuation
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.CoroutineContext

class MdcContinuationInterceptor : ContinuationInterceptor {
    override val key: CoroutineContext.Key<*>
        get() = ContinuationInterceptor

    override fun <T> interceptContinuation(continuation: Continuation<T>): Continuation<T> {
        return MdcContinuationInterceptor(continuation)
    }

    class MdcContinuationInterceptor<T>(private val continuation: Continuation<T>) : Continuation<T> {
        override val context: CoroutineContext
            get() = continuation.context

        override fun resumeWith(result: Result<T>) {
            continuation.context[ReactorContext]?.context?.get<String>(MDC_KEY_TRACE_ID)?.run {
                MDC.put(MDC_KEY_TRACE_ID, this)
            }
            continuation.resumeWith(result)
        }
    }
}
