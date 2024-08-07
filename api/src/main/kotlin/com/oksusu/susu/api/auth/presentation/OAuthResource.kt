package com.oksusu.susu.api.auth.presentation

import com.oksusu.susu.api.auth.application.OAuthFacade
import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.auth.model.request.OAuthLoginRequest
import com.oksusu.susu.api.auth.model.request.OAuthRegisterRequest
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.extension.wrapCreated
import com.oksusu.susu.api.extension.wrapOk
import com.oksusu.susu.api.user.model.UserDeviceContext
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.OAUTH_SWAGGER_TAG, description = "OAuth API")
@RestController
@RequestMapping(value = ["/api/v1/oauth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class OAuthResource(
    private val oAuthFacade: OAuthFacade,
) {

    /**
     * 가입된 유저인지 체크합니다. /n
     * APPLE 로그인은 accessToken에 idToken 넣어주세요
     */
    @Operation(summary = "register valid check")
    @GetMapping("/{provider}/sign-up/valid")
    suspend fun checkRegisterValid(
        @PathVariable provider: OAuthProvider,
        @RequestParam accessToken: String,
    ) = oAuthFacade.checkRegisterValid(provider, accessToken).wrapOk()

    /**
     * 회원가입을 합니다.
     * APPLE 로그인은 accessToken에 idToken 넣어주세요
     */
    @Operation(summary = "register")
    @PostMapping("/{provider}/sign-up")
    suspend fun register(
        deviceContext: UserDeviceContext,
        @PathVariable provider: OAuthProvider,
        @RequestBody request: OAuthRegisterRequest,
        @RequestParam accessToken: String,
    ) = oAuthFacade.register(provider, accessToken, request, deviceContext).wrapCreated()

    /**
     * 로그인을 합니다.
     * APPLE 로그인은 accessToken에 idToken 넣어주세요
     */
    @Operation(summary = "login")
    @PostMapping("/{provider}/login")
    suspend fun login(
        deviceContext: UserDeviceContext,
        @PathVariable provider: OAuthProvider,
        @RequestBody request: OAuthLoginRequest,
    ) = oAuthFacade.login(provider, request, deviceContext).wrapOk()

    @Operation(summary = "연동된 소셜 로그인 정보 조회")
    @GetMapping("/oauth")
    suspend fun getOAuthInfo(
        user: AuthUser,
    ) = oAuthFacade.getOAuthInfo(user).wrapOk()
}
