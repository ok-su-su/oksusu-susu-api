package com.oksusu.susu.common.config.reactor

import com.oksusu.susu.common.extension.copyToMdc
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.reactivestreams.Subscription
import org.springframework.context.annotation.Configuration
import reactor.core.CoreSubscriber
import reactor.core.publisher.Hooks
import reactor.core.publisher.Operators
import reactor.util.context.Context

/**
 * 리액터 쓰레드 변경시 MDC 값 전파하기 위해서 사용합니다.
 * @link https://www.novatec-gmbh.de/en/blog/how-can-the-mdc-context-be-used-in-the-reactive-spring-applications/
 */
@Configuration
class MdcContextLifterConfiguration {
    companion object {
        val MDC_CONTEXT_REACTOR_KEY: String = MdcContextLifterConfiguration::class.java.name
    }

    @PostConstruct
    fun contextOperatorHook() {
        Hooks.onEachOperator(
            MDC_CONTEXT_REACTOR_KEY,
            Operators.lift { _, subscriber ->
                MdcContextLifter<Any?>(subscriber)
            }
        )
    }

    @PreDestroy
    fun cleanupHook() {
        Hooks.resetOnEachOperator(MDC_CONTEXT_REACTOR_KEY)
    }
}

/**
 * Helper that copies the state of Reactor [Context] to MDC on the #onNext function.
 */
class MdcContextLifter<T>(
    private val coreSubscriber: CoreSubscriber<T>,
) : CoreSubscriber<T> {
    override fun onSubscribe(subscription: Subscription) {
        coreSubscriber.onSubscribe(subscription)
    }

    override fun onNext(t: T) {
        coreSubscriber.currentContext().copyToMdc()
        coreSubscriber.onNext(t)
    }

    override fun onError(throwable: Throwable) {
        coreSubscriber.onError(throwable)
    }

    override fun onComplete() {
        coreSubscriber.onComplete()
    }

    override fun currentContext(): Context {
        return coreSubscriber.currentContext()
    }
}
