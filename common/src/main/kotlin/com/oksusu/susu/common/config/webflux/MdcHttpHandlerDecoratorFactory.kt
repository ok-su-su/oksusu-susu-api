package com.oksusu.susu.common.config.webflux

import com.oksusu.susu.common.consts.MDC_KEY_TRACE_ID
import com.oksusu.susu.common.extension.insert
import io.github.oshai.kotlinlogging.KotlinLogging
import org.slf4j.MDC
import org.springframework.http.server.reactive.HttpHandler
import org.springframework.http.server.reactive.HttpHandlerDecoratorFactory
import org.springframework.stereotype.Component
import java.util.UUID

/**
 * 커스텀 httpHandler 팩토리입니다.
 * httpHandler.handle() 에서 로그가 남아서 그 이전에 MDC 값이 주입되도록 했습니다.
 */
@Component
class MdcHttpHandlerDecoratorFactory : HttpHandlerDecoratorFactory {
    val logger = KotlinLogging.logger { }
    override fun apply(httpHandler: HttpHandler): HttpHandler {
        return HttpHandler { request, response ->
            val uuid = UUID.randomUUID().toString()
            try {
                MDC.put(MDC_KEY_TRACE_ID, uuid)
                httpHandler.handle(request, response)
                    .contextWrite { it.insert(MDC_KEY_TRACE_ID, uuid) }
            } finally {
                MDC.clear()
            }
        }
    }
}
