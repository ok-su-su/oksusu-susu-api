package com.oksusu.susu.client.slack

import com.oksusu.susu.client.WebClientFactory
import com.oksusu.susu.client.slack.model.SlackMessageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody

internal class SlackService {
    companion object {
        private const val SLACK_WEBHOOKS_DOMAIN = "https://hooks.slack.com/services"
        private val webClient = WebClientFactory.generate(baseUrl = SLACK_WEBHOOKS_DOMAIN)

        fun sendMessage(message: SlackMessageModel, token: String) {
            CoroutineScope(Dispatchers.IO).launch {
                webClient.post()
                    .uri("/$token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .awaitBody()
            }
        }
    }
}
