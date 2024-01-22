package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OAuthFacade
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.request.OAuthLoginRequest
import com.oksusu.susu.auth.model.request.OauthRegisterRequest
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.OAUTH_SWAGGER_TAG)
@RestController
@RequestMapping(value = ["/api/v1/oauth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class OauthResource(
    private val oAuthFacade: OAuthFacade,
) {
    /** 가입된 유저인지 체크합니다. */
    @Operation(summary = "register valid check")
    @GetMapping("/{provider}/sign-up/valid")
    suspend fun checkRegisterValid(
        @PathVariable provider: OauthProvider,
        @RequestParam accessToken: String,
    ) = oAuthFacade.checkRegisterValid(provider, accessToken).wrapOk()

    /** 회원가입을 합니다. */
    @Operation(summary = "register")
    @PostMapping("/{provider}/sign-up")
    suspend fun register(
        @PathVariable provider: OauthProvider,
        @RequestBody oauthRegisterRequest: OauthRegisterRequest,
        @RequestParam accessToken: String,
    ) = oAuthFacade.register(provider, accessToken, oauthRegisterRequest).wrapCreated()

    /** 로그인을 합니다. */
    @Operation(summary = "login")
    @PostMapping("/{provider}/login")
    suspend fun login(
        @PathVariable provider: OauthProvider,
        @RequestBody request: OAuthLoginRequest,
    ) = oAuthFacade.login(provider, request).wrapOk()

    @Operation(summary = "연동된 소셜 로그인 정보 조회")
    @GetMapping("/oauth")
    suspend fun getOAuthInfo(
        user: AuthUser,
    ) = oAuthFacade.getOAuthInfo(user).wrapOk()
}
