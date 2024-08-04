package com.oksusu.susu.client.discord

import com.oksusu.susu.client.discord.model.DiscordMessageModel

interface DiscordClient {
    suspend fun sendSummary(message: DiscordMessageModel)
    suspend fun sendError(message: DiscordMessageModel)
    suspend fun sendMessage(message: DiscordMessageModel, token: String, withRecover: Boolean = true)
}
