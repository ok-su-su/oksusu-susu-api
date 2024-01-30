package com.oksusu.susu.slack.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "slack.alarm")
data class SlackAlarmConfig(
    val errorWebhook: ErrorWebhook,
) {
    sealed class SlackAlarmModel(
        open val url: String,
        open val text: String,
        open val userName: String,
    )

    data class ErrorWebhook(
        override val url: String,
        override val text: String,
        override val userName: String,
    ) : SlackAlarmModel(url, text, userName)
}
