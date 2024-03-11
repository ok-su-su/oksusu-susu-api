package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.SystemActionLogEvent
import com.oksusu.susu.api.log.application.SystemActionLogService
import com.oksusu.susu.api.log.domain.SystemActionLog
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SystemActionLogEventListener(
    private val systemActionLogService: SystemActionLogService,
) {
    @EventListener
    fun subscribe(event: SystemActionLogEvent) {
        CoroutineScope(Dispatchers.IO + Job()).launch {
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
