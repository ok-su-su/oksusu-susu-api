package com.oksusu.susu.client.slack

import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.extension.withMDCContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class SuspendableSlackClient(
    private val webclient: WebClient,
    private val slackWebhookConfig: SusuConfig.SlackWebhookConfig,
) : SlackClient {
    override suspend fun sendSummary(message: SlackMessageModel): String {
        return withContext(Dispatchers.IO.withMDCContext()) {
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
