package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OauthService
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.extension.wrapOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Dev Oauth")
@RestController
@RequestMapping(value = ["/api/v1/dev/oauth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DevOAuthResource(
    private val oauthService: OauthService,
) {
    /** oauth login link를 반환해줍니다. 개발용 */
    @Operation(summary = "oauth link", deprecated = true)
    @GetMapping("/{provider}/link")
    suspend fun getOauthLoginLinkDev(
        @PathVariable provider: OauthProvider,
    ) = oauthService.getOauthLoginLinkDev(provider).wrapOk()

    /** oauth 토큰 받아옵니다. 개발용 */
    @Operation(summary = "oauth link", deprecated = true)
    @GetMapping("/{provider}/token")
    suspend fun getOauthLogin(
        @PathVariable provider: OauthProvider,
        @RequestParam code: String,
    ) = oauthService.getOauthTokenDev(provider, code).wrapOk()
}
