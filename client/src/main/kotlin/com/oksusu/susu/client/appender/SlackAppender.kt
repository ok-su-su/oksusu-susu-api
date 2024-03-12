package com.oksusu.susu.client.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.oksusu.susu.client.WebClientFactory
import com.oksusu.susu.client.slack.model.SlackMessageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody

class SlackAppender : AppenderBase<ILoggingEvent>() {
    private var token: String = ""

    fun setToken(token: String) {
        this.token = token
    }

    override fun append(event: ILoggingEvent) {
        val message = SlackMessageModel(
            text = """
                        $event
            """.trimIndent()
        )

        WarningLogService.sendLog(message, token)
    }
}

class WarningLogService {
    companion object {
        private const val SLACK_WEBHOOKS_DOMAIN = "https://hooks.slack.com/services"
        private val webClient = WebClientFactory.generate(baseUrl = SLACK_WEBHOOKS_DOMAIN)

        fun sendLog(message: SlackMessageModel, token: String) {
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

