package com.oksusu.susu.api.slack.application

import com.oksusu.susu.client.config.SlackConfig
import com.oksusu.susu.common.extension.isProd
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.api.slack.infrastructure.SlackAlarmSender
import com.oksusu.susu.api.slack.model.ErrorWebhookDataModel
import com.slack.api.model.block.LayoutBlock
import com.slack.api.webhook.Payload
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class SuspendableSlackAlarmService(
    private val slackAlarmSender: SlackAlarmSender,
    private val slackBlockHelper: SlackBlockHelper,
    private val environment: Environment,
    private val slackErrorWebhookConfig: SlackConfig.SlackErrorWebhookConfig
) {
    val logger = KotlinLogging.logger {}

    suspend fun sendSlackErrorAlarm(data: ErrorWebhookDataModel) {
        /** prod 환경에서만 작동 */
        if (!environment.isProd()) {
            return
        }

        val layoutBlocks = slackBlockHelper.getErrorBlocks(data)

        return sendAlarm(slackErrorWebhookConfig, layoutBlocks)
    }

    private suspend fun sendAlarm(model: SlackConfig.SlackAlarmModel, layoutBlocks: List<LayoutBlock>) {
        /** prod 환경에서만 작동 */
        if (!environment.isProd()) {
            return
        }

        val payload = Payload.builder()
            .text(model.text)
            .username(model.userName)
            .blocks(layoutBlocks)
            .build()

        withMDCContext(Dispatchers.IO) {
            slackAlarmSender.send(model.url, payload)
        }
    }
}
