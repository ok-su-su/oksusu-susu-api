package com.oksusu.susu.event.listener

import com.oksusu.susu.event.model.SystemActionLogEvent
import com.oksusu.susu.log.application.SystemActionLogService
import com.oksusu.susu.log.domain.SystemActionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SystemActionLogEventListener(
    private val systemActionLogService: SystemActionLogService,
) {
    @EventListener
    fun subscribe(event: SystemActionLogEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            SystemActionLog(
                ipAddress = event.ipAddress,
                path = event.path,
                httpMethod = event.method,
                userAgent = event.userAgent,
                host = event.host,
                referer = event.referer,
                extra = event.extra
            ).run { systemActionLogService.record(this) }
        }
    }
}
