package com.oksusu.susu.client.slack

import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.config.SusuConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody

class SuspendableSlackClient(
    private val webclient: WebClient,
    private val slackWebhookConfig: SusuConfig.SlackWebhookConfig,
) : SlackClient {
    override suspend fun send(message: SlackMessageModel): String {
        return withContext(Dispatchers.IO) {
            webclient
                .post()
                .uri("/${slackWebhookConfig.token}")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(message)
                .retrieve()
                .awaitBody()
        }
    }
}
