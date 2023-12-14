package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OauthService
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "Oauth")
@RestController
@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class OauthResource(
    private val oauthService: OauthService
) {
    private val logger = mu.KotlinLogging.logger { }

    /** oauth login link를 반환해줍니다. 개발용 */
    @Operation(summary = "oauth link", deprecated = true)
    @GetMapping("/v1/oauth/{provider}/link/dev")
    suspend fun getOauthLoginLinkDev(@PathVariable provider: OauthProvider): OauthLoginLinkResponse {
        return oauthService.getOauthLoginLinkDev(provider)
    }

    /** oauth 토큰 받아옵니다. 개발용 */
    @Operation(summary = "oauth link", deprecated = true)
    @GetMapping("/v1/oauth/{provider}/token/dev")
    suspend fun getOauthLogin(
        @PathVariable provider: OauthProvider,
        @RequestParam code: String,
    ): OauthTokenResponse {
        return oauthService.getOauthTokenDev(provider, code)
    }

    /** 가입된 유저인지 체크합니다. */
    @Operation(summary = "register valid check")
    @GetMapping("/v1/oauth/{provider}/register/valid")
    suspend fun checkRegisterValid(
        @PathVariable provider: OauthProvider,
        @RequestParam accessToken: String,
    ): AbleRegisterResponse {
        return oauthService.checkRegisterValid(provider, accessToken)
    }

    /** 회원가입을 합니다. */
    @Operation(summary = "register")
    @PostMapping("/v1/oauth/{provider}/register")
    suspend fun register(
        @PathVariable provider: OauthProvider,
        @RequestBody oauthRegisterRequest: OauthRegisterRequest,
        @RequestParam accessToken: String,
    ): TokenDto {
        return oauthService.register(provider, accessToken, oauthRegisterRequest)
    }

    /** 로그인을 합니다. */
    @Operation(summary = "login")
    @PostMapping("/v1/oauth/{provider}/login")
    suspend fun login(
        @PathVariable provider: OauthProvider,
        @RequestParam accessToken: String,
    ): TokenDto {
        return oauthService.login(provider, accessToken)
    }
}
