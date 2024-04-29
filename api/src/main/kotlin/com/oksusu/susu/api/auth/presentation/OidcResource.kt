package com.oksusu.susu.api.auth.presentation

import com.oksusu.susu.api.auth.application.OAuthFacade
import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.auth.model.request.OAuthLoginRequest
import com.oksusu.susu.api.auth.model.request.OAuthRegisterRequest
import com.oksusu.susu.api.auth.model.request.OidcLoginRequest
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.extension.wrapCreated
import com.oksusu.susu.api.extension.wrapOk
import com.oksusu.susu.api.user.model.UserDeviceContext
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.OAUTH_OIDC_SWAGGER_TAG, description = "OAuth Oidc API")
@RestController
@RequestMapping(value = ["/api/v1/oauth/oidc"], produces = [MediaType.APPLICATION_JSON_VALUE])
class OidcResource(
    private val oAuthFacade: OAuthFacade,
) {

    /** 가입된 유저인지 체크합니다. (현재 Google만 지원) */
    @Operation(summary = "register valid check")
    @GetMapping("/{provider}/sign-up/valid")
    suspend fun checkRegisterValid(
        @PathVariable provider: OAuthProvider,
        @RequestParam idToken: String,
    ) = oAuthFacade.checkRegisterValidWithOidc(provider, idToken).wrapOk()

    /** 회원가입을 합니다. (현재 Google만 지원) */
    @Operation(summary = "register")
    @PostMapping("/{provider}/sign-up")
    suspend fun register(
        deviceContext: UserDeviceContext,
        @PathVariable provider: OAuthProvider,
        @RequestBody request: OAuthRegisterRequest,
        @RequestParam idToken: String,
    ) = oAuthFacade.registerWithOidc(provider, idToken, request, deviceContext).wrapCreated()

    /** OIDC 로그인을 합니다. (현재 Google만 지원) */
    @Operation(summary = "login oidc")
    @PostMapping("/{provider}/login")
    suspend fun loginWithOidc(
        deviceContext: UserDeviceContext,
        @PathVariable provider: OAuthProvider,
        @RequestBody request: OidcLoginRequest,
    ) = oAuthFacade.loginWithOidc(provider, request, deviceContext).wrapOk()
}
