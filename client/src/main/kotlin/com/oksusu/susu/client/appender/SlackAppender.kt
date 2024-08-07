package com.oksusu.susu.client.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.oksusu.susu.client.slack.SlackService
import com.oksusu.susu.client.slack.model.SlackMessageModel

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

        SlackService.sendMessage(message, token)
    }
}
