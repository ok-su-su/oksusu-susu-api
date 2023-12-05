package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OauthService
import com.oksusu.susu.auth.model.OauthProvider
import com.oksusu.susu.auth.model.dto.OauthLoginLinkResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Oauth")
@RestController
@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class OauthResource(
    private val oauthService: OauthService
) {

    /** oauth login link를 반환해줍니다. 개발용 입니다 */
    @Operation(summary = "oauth link", deprecated = true)
    @GetMapping("/v1/oauth/{provider}/link")
    fun getOauthLoginLinkDev(@PathVariable provider: OauthProvider): OauthLoginLinkResponse {
        return oauthService.getOauthLoginLinkDev(provider)
    }

}
