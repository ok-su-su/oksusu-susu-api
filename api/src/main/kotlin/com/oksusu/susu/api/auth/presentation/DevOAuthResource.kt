package com.oksusu.susu.api.auth.presentation

import com.oksusu.susu.api.auth.application.DevOAuthService
import com.oksusu.susu.api.auth.model.OAuthProvider
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.extension.wrapOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.DEV_OAUTH_SWAGGER_TAG, description = "개발용 OAuth API")
@RestController
@RequestMapping(value = ["/api/v1/dev/oauth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DevOAuthResource(
    private val devOAuthService: DevOAuthService,
) {
    /** oauth login link를 반환해줍니다. 개발용 */
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "dev oauth link")
    @GetMapping("/{provider}/link")
    suspend fun getDevOAuthLoginLink(
        @PathVariable provider: OAuthProvider,
    ) = devOAuthService.getOAuthLoginLinkDev(provider).wrapOk()

    /** oauth 토큰 받아옵니다. 개발용 */
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "dev oauth link")
    @GetMapping("/{provider}/token")
    suspend fun getDevOAuthLogin(
        @PathVariable provider: OAuthProvider,
        @RequestParam code: String,
    ) = devOAuthService.getOAuthTokenDev(provider, code).wrapOk()
}
