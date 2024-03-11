package com.oksusu.susu.api.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.oksusu.susu.api.client.slack.model.SlackMessageModel
import com.oksusu.susu.api.log.application.WarningLogService

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

        WarningLogService.sendWarningLog(message, token)
    }
}
