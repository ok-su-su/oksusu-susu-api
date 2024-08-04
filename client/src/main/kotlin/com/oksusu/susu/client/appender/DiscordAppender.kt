package com.oksusu.susu.client.appender

import ch.qos.logback.classic.spi.ILoggingEvent
import ch.qos.logback.core.AppenderBase
import com.oksusu.susu.client.discord.DiscordService
import com.oksusu.susu.client.discord.model.DiscordMessageModel
import com.oksusu.susu.client.slack.model.SlackMessageModel

class DiscordAppender : AppenderBase<ILoggingEvent>() {
    private var token: String = ""

    fun setToken(token: String) {
        this.token = token
    }

    override fun append(event: ILoggingEvent) {
        val message = DiscordMessageModel(
            content = """
                        $event
            """.trimIndent()
        )

        DiscordService.sendMessage(message, token)
    }
}
