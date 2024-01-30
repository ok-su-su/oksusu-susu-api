package com.oksusu.susu.slack.infrastructure

import com.slack.api.Slack
import com.slack.api.webhook.Payload
import org.springframework.stereotype.Component

@Component
class SlackAlarmSender {
    fun send(url: String, payload: Payload) {
        val client = Slack.getInstance()

        client.send(url, payload)
    }
}
