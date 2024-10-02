package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.auth.application.AuthFacade
import com.oksusu.susu.api.auth.model.AuthUserToken
import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.SystemActionLogEvent
import com.oksusu.susu.api.log.application.SystemActionLogService
import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.extension.mdcCoroutineScope
import com.oksusu.susu.domain.log.domain.SystemActionLog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener

@SusuEventListener
class SystemActionLogEventListener(
    private val systemActionLogService: SystemActionLogService,
    private val coroutineExceptionHandler: ErrorPublishingCoroutineExceptionHandler,
    private val authFacade: AuthFacade,
) {
    @EventListener
    fun subscribe(event: SystemActionLogEvent) {
        if (check(event)) {
            return
        }

        mdcCoroutineScope(Dispatchers.IO + Job() + coroutineExceptionHandler.handler, event.traceId).launch {
            val uid = event.token
                ?.let { token -> AuthUserToken.from(token) }
                ?.let { token -> authFacade.getUidFromTokenOrNull(token) }

            SystemActionLog(
                uid = uid,
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

    private fun check(event: SystemActionLogEvent): Boolean {
        return NON_TARGET_PATH.contains(event.path)
    }

    companion object {
        private val NON_TARGET_PATH = setOf(
            "/api/v1/health",
            "/health"
        )
    }
}
