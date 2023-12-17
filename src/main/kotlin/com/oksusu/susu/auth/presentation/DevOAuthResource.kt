package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OAuthService
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.extension.wrapOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "Dev Oauth")
@RestController
@RequestMapping(value = ["/api/v1/dev/oauth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DevOAuthResource(
    private val oauthService: OAuthService,
) {
    /** oauth login link를 반환해줍니다. 개발용 */
    @Operation(summary = "dev oauth link", deprecated = true)
    @GetMapping("/{provider}/link")
    suspend fun getDevOAuthLoginLink(
        @PathVariable provider: OauthProvider,
    ) = oauthService.getOauthLoginLinkDev(provider).wrapOk()

    /** oauth 토큰 받아옵니다. 개발용 */
    @Operation(summary = "dev oauth link", deprecated = true)
    @GetMapping("/{provider}/token")
    suspend fun getDevOauthLogin(
        @PathVariable provider: OauthProvider,
        @RequestParam code: String,
    ) = oauthService.getOauthTokenDev(provider, code).wrapOk()
}
