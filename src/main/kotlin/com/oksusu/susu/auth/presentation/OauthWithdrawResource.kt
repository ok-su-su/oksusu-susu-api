package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.OAuthFacade
import com.oksusu.susu.auth.application.OAuthService
import com.oksusu.susu.auth.model.OauthProvider
import org.springframework.http.server.reactive.AbstractServerHttpRequest
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.result.view.RedirectView

@Controller
@RequestMapping
class OauthWithdrawResource(
    private val oAuthService: OAuthService,
    private val oAuthFacade: OAuthFacade,
) {
    /** 회원 탈퇴용 로그인 페이지 */
    @GetMapping("/withdraw/login")
    suspend fun getWithdrawLoginPage(
        model: Model,
        serverHttpRequest: ServerHttpRequest,
    ): String {
        val request = serverHttpRequest as AbstractServerHttpRequest
        val kakaoRedirectUrl = oAuthService.getOauthLoginLink(OauthProvider.KAKAO, request.uri.toString()).link
        model.addAttribute("kakaoRedirectUrl", kakaoRedirectUrl)
        return "withdrawLogin"
    }

    /** oauth callback용 url, 로그인 완료되면 /withdraw로 redirect */
    @GetMapping("kakao/callback")
    suspend fun getKakaoCallbackPage(
        model: Model,
        serverHttpRequest: ServerHttpRequest,
        @RequestParam code: String,
    ): RedirectView {
        val request = serverHttpRequest as AbstractServerHttpRequest
        val susuToken = oAuthFacade.loginWithCode(OauthProvider.KAKAO, code, request)
        return RedirectView("/withdraw?xSusuAuthToken=$susuToken")
    }

    /** 회원 탈퇴 페이지 */
    @GetMapping("/withdraw")
    suspend fun getWithdrawPage(
        model: Model,
        @RequestParam xSusuAuthToken: String,
    ): String {
        model.addAttribute("xSusuAuthToken", xSusuAuthToken)
        return "withdraw"
    }
}
