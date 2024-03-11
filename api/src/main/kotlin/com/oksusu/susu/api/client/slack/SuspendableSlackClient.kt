package com.oksusu.susu.api.client.slack

import com.oksusu.susu.api.client.slack.model.SlackMessageModel
import com.oksusu.susu.api.config.SusuConfig
import com.oksusu.susu.api.extension.withMDCContext
import kotlinx.coroutines.Dispatchers
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class SuspendableSlackClient(
    private val webclient: WebClient,
    private val slackWebhookConfig: SusuConfig.SlackWebhookConfig,
) : SlackClient {
    override suspend fun sendSummary(message: SlackMessageModel): String {
        return withMDCContext(Dispatchers.IO) {
            webclient
                .post()
                .uri("/${slackWebhookConfig.summaryToken}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .awaitBody()
        }
    }
}
