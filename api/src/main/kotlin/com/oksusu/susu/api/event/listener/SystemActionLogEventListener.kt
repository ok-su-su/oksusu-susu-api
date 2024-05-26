package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.SystemActionLogEvent
import com.oksusu.susu.api.log.application.SystemActionLogService
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.log.domain.SystemActionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener

@SusuEventListener
class SystemActionLogEventListener(
    private val systemActionLogService: SystemActionLogService,
) {
    @EventListener
    fun subscribe(event: SystemActionLogEvent) {
        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
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
