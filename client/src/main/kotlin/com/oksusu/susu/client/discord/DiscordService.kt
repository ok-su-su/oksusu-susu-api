package com.oksusu.susu.client.discord

import com.oksusu.susu.client.WebClientFactory
import com.oksusu.susu.client.discord.model.DiscordMessageModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.http.MediaType
import org.springframework.web.reactive.function.client.awaitBody

internal class DiscordService {
    companion object {
        private const val DISCORD_WEBHOOKS_DOMAIN = "https://discord.com/api/webhooks"
        private val webClient = WebClientFactory.generate(baseUrl = DISCORD_WEBHOOKS_DOMAIN)

        fun sendMessage(message: DiscordMessageModel, token: String) {
            CoroutineScope(Dispatchers.IO).launch {
                webClient.post()
                    .uri("/$token")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(message)
                    .retrieve()
                    .awaitBody()
            }
        }
    }
}
