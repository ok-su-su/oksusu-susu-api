package com.oksusu.susu.client.slack

import com.oksusu.susu.client.slack.model.SlackMessageModel

interface SlackClient {
    suspend fun sendSummary(message: SlackMessageModel): String
    suspend fun sendError(message: SlackMessageModel): String
    suspend fun sendMessage(message: SlackMessageModel, token: String, withRecover: Boolean = true): String
}
