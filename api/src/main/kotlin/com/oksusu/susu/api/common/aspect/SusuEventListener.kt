package com.oksusu.susu.api.common.aspect

import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression
import org.springframework.stereotype.Component

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@Component
@ConditionalOnExpression("\${server.event-processing.enabled:true}")
annotation class SusuEventListener
