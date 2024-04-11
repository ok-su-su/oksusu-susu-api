package com.oksusu.susu.client.oauth.apple

import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.apple.model.AppleOAuthTokenResponse
import com.oksusu.susu.client.oauth.oidc.model.OidcPublicKeysResponse
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.web.reactive.function.client.WebClient

class SuspendableAppleClient(
    private val webClient: WebClient,
    private val appleOAuthUrlConfig: OAuthUrlConfig.AppleOAuthUrlConfig,
) : AppleClient {
    private val logger = KotlinLogging.logger { }

    override suspend fun getToken(
        redirectUrl: String,
        code: String,
        clientId: String,
        clientSecret: String,
    ): AppleOAuthTokenResponse {
        val url = appleOAuthUrlConfig.appleIdUrl + String.format(
            appleOAuthUrlConfig.tokenUrl,
            clientId,
            redirectUrl,
            code,
            clientSecret
        )
        return webClient.post()
            .uri(url)
            .retrieve()
            .bodyToMono(AppleOAuthTokenResponse::class.java)
            .awaitSingle()
    }

    override suspend fun getOidcPublicKeys(): OidcPublicKeysResponse {
        val url = appleOAuthUrlConfig.appleIdUrl + appleOAuthUrlConfig.oidcKeyUrl
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(OidcPublicKeysResponse::class.java)
            .awaitSingle()
    }

    override suspend fun withdraw(clientId: String, clientSecret: String, token: String) {
        val url = appleOAuthUrlConfig.appleIdUrl + String.format(
            appleOAuthUrlConfig.withdrawUrl,
            clientId,
            clientSecret,
            token
        )
        return webClient.post()
            .uri(url)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .awaitSingle()
    }
}
