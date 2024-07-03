package com.oksusu.susu.client.oauth.apple

import com.oksusu.susu.client.config.OAuthUrlConfig
import com.oksusu.susu.client.oauth.apple.model.AppleOAuthTokenResponse
import com.oksusu.susu.client.oauth.oidc.model.OidcPublicKeysResponse
import com.oksusu.susu.common.extension.awaitSingleOptionalOrThrow
import com.oksusu.susu.common.extension.awaitSingleOrThrow
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.MediaType
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
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .retrieve()
            .bodyToMono(AppleOAuthTokenResponse::class.java)
            .awaitSingleOrThrow()
    }

    override suspend fun getOidcPublicKeys(): OidcPublicKeysResponse {
        val url = appleOAuthUrlConfig.appleIdUrl + appleOAuthUrlConfig.oidcKeyUrl
        return webClient.get()
            .uri(url)
            .retrieve()
            .bodyToMono(OidcPublicKeysResponse::class.java)
            .awaitSingleOrThrow()
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
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .retrieve()
            .bodyToMono(Unit::class.java)
            .awaitSingleOptionalOrThrow() ?: Unit
    }
}
