package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OauthService
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.*
import com.oksusu.susu.auth.model.dto.request.OAuthLoginRequest
import com.oksusu.susu.auth.model.dto.request.OauthRegisterRequest
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "Oauth")
@RestController
@RequestMapping(value = ["/api/v1/oauth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class OauthResource(
    private val oauthService: OauthService,
) {
    /** 가입된 유저인지 체크합니다. */
    @Operation(summary = "register valid check")
    @GetMapping("/{provider}/sign-up/valid")
    suspend fun checkRegisterValid(
        @PathVariable provider: OauthProvider,
        @RequestParam accessToken: String,
    ) = oauthService.checkRegisterValid(provider, accessToken).wrapOk()

    /** 회원가입을 합니다. */
    @Operation(summary = "register")
    @PostMapping("/{provider}/sign-up")
    suspend fun register(
        @PathVariable provider: OauthProvider,
        @RequestBody oauthRegisterRequest: OauthRegisterRequest,
        @RequestParam accessToken: String,
    ) = oauthService.register(provider, accessToken, oauthRegisterRequest).wrapCreated()

    /** 로그인을 합니다. */
    @Operation(summary = "login")
    @PostMapping("/{provider}/login")
    suspend fun login(
        @PathVariable provider: OauthProvider,
        @RequestBody request: OAuthLoginRequest,
    ) = oauthService.login(provider, request).wrapOk()
}
