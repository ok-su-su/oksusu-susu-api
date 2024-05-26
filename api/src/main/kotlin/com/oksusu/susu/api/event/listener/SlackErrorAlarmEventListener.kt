package com.oksusu.susu.api.event.listener

import com.oksusu.susu.api.common.aspect.SusuEventListener
import com.oksusu.susu.api.event.model.SlackErrorAlarmEvent
import com.oksusu.susu.api.slack.application.SuspendableSlackAlarmService
import com.oksusu.susu.api.slack.model.ErrorWebhookDataModel
import com.oksusu.susu.common.extension.isProd
import com.oksusu.susu.common.extension.mdcCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.core.env.Environment

@SusuEventListener
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

        mdcCoroutineScope(Dispatchers.IO + Job(), event.traceId).launch {
            suspendableSlackAlarmService.sendSlackErrorAlarm(
                ErrorWebhookDataModel(
                    request = event.request,
                    exception = event.exception
                )
            )
        }
    }
}
