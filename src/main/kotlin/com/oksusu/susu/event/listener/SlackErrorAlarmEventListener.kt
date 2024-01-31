package com.oksusu.susu.event.listener

import com.oksusu.susu.event.model.SlackErrorAlarmEvent
import com.oksusu.susu.slack.application.SuspendableSlackAlarmService
import com.oksusu.susu.slack.model.ErrorWebhookDataModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SlackErrorAlarmEventListener(
    private val suspendableSlackAlarmService: SuspendableSlackAlarmService,
) {
    @EventListener
    fun execute(event: SlackErrorAlarmEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            suspendableSlackAlarmService.sendSlackErrorAlarm(
                ErrorWebhookDataModel(
                    request = event.request,
                    exception = event.exception
                )
            )
        }
    }
}
