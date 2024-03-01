package com.oksusu.susu.common.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.log.application.WarningLogService

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
