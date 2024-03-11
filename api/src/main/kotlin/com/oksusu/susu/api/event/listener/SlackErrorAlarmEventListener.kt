package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.event.model.SlackErrorAlarmEvent
import com.oksusu.susu.api.extension.isProd
import com.oksusu.susu.api.slack.application.SuspendableSlackAlarmService
import com.oksusu.susu.api.slack.model.ErrorWebhookDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment
import org.springframework.stereotype.Component

@Component
class SlackErrorAlarmEventListener(
    private val environment: Environment,
    private val suspendableSlackAlarmService: SuspendableSlackAlarmService,
) {
    @EventListener
    fun execute(event: SlackErrorAlarmEvent) {
        /** prod 환경에서만 작동 */
        if (!environment.isProd()) {
            return
        }

        CoroutineScope(Dispatchers.IO + Job()).launch {
            suspendableSlackAlarmService.sendSlackErrorAlarm(
                ErrorWebhookDataModel(
                    request = event.request,
                    exception = event.exception
                )
            )
        }
    }
}
