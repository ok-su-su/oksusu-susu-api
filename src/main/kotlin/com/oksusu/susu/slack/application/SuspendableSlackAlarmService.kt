package com.oksusu.susu.slack.application

import com.oksusu.susu.slack.config.SlackAlarmConfig
import com.oksusu.susu.slack.infrastructure.SlackAlarmSender
import com.oksusu.susu.slack.model.ErrorWebhookDataModel
import com.slack.api.model.block.LayoutBlock
import com.slack.api.webhook.Payload
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class SuspendableSlackAlarmService(
    private val slackAlarmSender: SlackAlarmSender,
    private val slackBlockHelper: SlackBlockHelper,
    private val environment: Environment,
) {
    suspend fun sendSlackErrorAlarm(model: SlackAlarmConfig.SlackAlarmModel, data: ErrorWebhookDataModel) {
        val layoutBlocks = slackBlockHelper.getErrorBlocks(data)

        return sendAlarm(model, layoutBlocks)
    }

    private suspend fun sendAlarm(model: SlackAlarmConfig.SlackAlarmModel, layoutBlocks: List<LayoutBlock>) {
        /** prod 환경에서만 작동 */
//        if (!environment.isProd()) {
//            return
//        }

        val payload = Payload.builder()
            .text(model.text)
            .username(model.userName)
            .blocks(layoutBlocks)
            .build()

        withContext(Dispatchers.IO) {
            slackAlarmSender.send(model.url, payload)
        }
    }
}
