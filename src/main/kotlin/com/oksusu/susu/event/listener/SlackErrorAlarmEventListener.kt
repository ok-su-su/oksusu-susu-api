package com.oksusu.susu.event.listener

import com.oksusu.susu.event.model.SlackErrorAlarmEvent
import com.oksusu.susu.slack.application.SuspendableSlackAlarmService
import com.oksusu.susu.slack.config.SlackAlarmConfig
import com.oksusu.susu.slack.model.ErrorWebhookDataModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.context.event.EventListener
import org.springframework.stereotype.Component

@Component
class SlackErrorAlarmEventListener(
    private val slackAlarmConfig: SlackAlarmConfig,
    private val suspendableSlackAlarmService: SuspendableSlackAlarmService,
) {
    val logger = KotlinLogging.logger { }

    @EventListener
    fun execute(event: SlackErrorAlarmEvent) {
        CoroutineScope(Dispatchers.IO).launch {
            suspendableSlackAlarmService.sendSlackErrorAlarm(
                slackAlarmConfig.errorWebhook,
                ErrorWebhookDataModel(
                    request = event.request,
                    exception = event.exception
                )
            )
        }
    }
}
