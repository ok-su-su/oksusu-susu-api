import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.annotation.PostConstruct
import jakarta.annotation.PreDestroy
import org.reactivestreams.Subscription
import org.slf4j.MDC
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.Order
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.CoreSubscriber
import reactor.core.publisher.Hooks
import reactor.core.publisher.Mono
import reactor.core.publisher.Operators
import reactor.util.context.Context
import java.util.UUID
import java.util.stream.Collectors

@Configuration
class MdcContextLifterConfig {
    companion object {
        val MDC_CONTEXT_REACTOR_KEY: String = MdcContextLifterConfig::class.java.name
    }

    @PostConstruct
    fun contextOperatorHook() {
        Hooks.onEachOperator(MDC_CONTEXT_REACTOR_KEY, Operators.lift { _, subscriber -> MdcContextLifter(subscriber) })
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
    private val coreSubscriber: CoreSubscriber<T>
) : CoreSubscriber<T> {
    override fun onNext(t: T) {
        coreSubscriber.currentContext().copyToMdc()
        coreSubscriber.onNext(t)
    }

    override fun onSubscribe(subscription: Subscription) {
        coreSubscriber.onSubscribe(subscription)
    }

    override fun onComplete() {
        coreSubscriber.onComplete()
    }

    override fun onError(throwable: Throwable?) {
        coreSubscriber.onError(throwable)
    }

    override fun currentContext(): Context {
        return coreSubscriber.currentContext()
    }
}

/**
 * Extension function for the Reactor [Context]. Copies the current context to the MDC, if context is empty clears the MDC.
 * State of the MDC after calling this method should be same as Reactor [Context] state.
 * One thread-local access only.
 */
private fun Context.copyToMdc() {
    if (!this.isEmpty) {
        val map: Map<String, String> = this.stream()
            .collect(Collectors.toMap({ e -> e.key.toString() }, { e -> e.value.toString() }))

        MDC.setContextMap(map)
    } else {
        MDC.clear()
    }
}

private fun Context.toMap(): Map<Any, Any> = this.stream()
    .collect(Collectors.toMap({ e -> e.key.toString() }, { e -> e.value.toString() }))

@Component
class MdcLoggingFilter : WebFilter {
    private val logger = KotlinLogging.logger { }

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return chain.filter(exchange)
            .contextWrite { context ->
                val key = UUID.randomUUID().toString().substring(0, 7)
                logger.debug { "requestId $key" }
                context.withMDC("requestId", key)
            }
    }
}

fun Context.withMDC(key: Any, value: Any): Context {
    val mapOfContext = this.toMap().toMutableMap()
    mapOfContext[key] = value
    MDC.put(key.toString(), value.toString())
    return Context.of(mapOfContext)
}
