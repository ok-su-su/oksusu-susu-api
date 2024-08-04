package com.oksusu.susu.api.event.model

import com.oksusu.susu.client.discord.model.DiscordMessageModel
import com.oksusu.susu.client.slack.model.SlackMessageModel
import com.oksusu.susu.common.extension.format
import java.time.LocalDateTime

internal data class ErrorMessage(
    val url: String,
    val method: String,
    val errorMessage: String,
    val errorStack: String,
    val errorUserIP: String,
    val errorRequestParam: String,
    val body: String,
) {
    fun slackMessage(): SlackMessageModel {
        return SlackMessageModel(
            """
                **[ 에러 발생 ${LocalDateTime.now().format("yyyy-MM-dd HH:mm:ss")} ]**
                - User IP : $errorUserIP
                - Request Addr : $method - $url
                - Requset Param : $errorRequestParam
                - Request Body : $body
                - Message : $errorMessage
                - Stack Trace : $errorStack
            """.trimIndent()
        )
    }

    fun discordMessage(): DiscordMessageModel {
        return DiscordMessageModel(
            """
                **[ 에러 발생 ${LocalDateTime.now().format("yyyy-MM-dd HH:mm:ss")} ]**
                - User IP : $errorUserIP
                - Request Addr : $method - $url
                - Requset Param : $errorRequestParam
                - Request Body : $body
                - Message : $errorMessage
                - Stack Trace : $errorStack
            """.trimIndent()
        )
    }
}

