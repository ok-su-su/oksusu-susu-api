package com.oksusu.susu.api.client.slack

import com.oksusu.susu.api.client.slack.model.SlackMessageModel

interface SlackClient {
    suspend fun sendSummary(message: SlackMessageModel): String
}
