package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OauthService
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.*
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
    private val oauthService: OauthService
) {
    private val logger = mu.KotlinLogging.logger { }

    /** oauth login link를 반환해줍니다. 개발용 */
    @Operation(summary = "oauth link", deprecated = true)
    @GetMapping("/{provider}/link/dev")
    suspend fun getOauthLoginLinkDev(
        @PathVariable provider: OauthProvider,
    ) = oauthService.getOauthLoginLinkDev(provider).wrapOk()

    /** oauth 토큰 받아옵니다. 개발용 */
    @Operation(summary = "oauth link", deprecated = true)
    @GetMapping("/{provider}/token/dev")
    suspend fun getOauthLogin(
        @PathVariable provider: OauthProvider,
        @RequestParam code: String,
    ) = oauthService.getOauthTokenDev(provider, code).wrapOk()

    /** 가입된 유저인지 체크합니다. */
    @Operation(summary = "register valid check")
    @GetMapping("/{provider}/register/valid")
    suspend fun checkRegisterValid(
        @PathVariable provider: OauthProvider,
        @RequestParam accessToken: String,
    ) = oauthService.checkRegisterValid(provider, accessToken).wrapOk()

    /** 회원가입을 합니다. */
    @Operation(summary = "register")
    @PostMapping("/{provider}/register")
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
        @RequestParam accessToken: String,
    ) = oauthService.login(provider, accessToken).wrapOk()
}
