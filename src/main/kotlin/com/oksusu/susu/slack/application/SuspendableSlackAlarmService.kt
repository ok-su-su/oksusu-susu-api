package com.oksusu.susu.slack.application

import com.oksusu.susu.config.SlackConfig
import com.oksusu.susu.extension.isProd
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.slack.infrastructure.SlackAlarmSender
import com.oksusu.susu.slack.model.ErrorWebhookDataModel
import com.slack.api.model.block.LayoutBlock
import com.slack.api.webhook.Payload
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.core.env.Environment
import org.springframework.stereotype.Service

@Service
class SuspendableSlackAlarmService(
    private val slackAlarmSender: SlackAlarmSender,
    private val slackBlockHelper: SlackBlockHelper,
    private val environment: Environment,
    private val errorWebhookConfig: SlackConfig.ErrorWebhookConfig,
) {
    val logger = KotlinLogging.logger {}

    suspend fun sendSlackErrorAlarm(data: ErrorWebhookDataModel) {
        /** prod 환경에서만 작동 */
        if (!environment.isProd()) {
            return
        }

        val layoutBlocks = slackBlockHelper.getErrorBlocks(data)

        return sendAlarm(errorWebhookConfig, layoutBlocks)
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

        withContext(Dispatchers.IO.withMDCContext()) {
            slackAlarmSender.send(model.url, payload)
        }
    }
}
