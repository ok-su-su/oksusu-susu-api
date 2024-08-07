package com.oksusu.susu.api.auth.presentation

import com.oksusu.susu.api.auth.application.OAuthFacade
import com.oksusu.susu.api.auth.application.OAuthService
import com.oksusu.susu.api.auth.model.request.OAuthLoginRequest
import com.oksusu.susu.api.user.model.UserDeviceContextImpl
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import org.springframework.http.server.reactive.ServerHttpRequest
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.reactive.result.view.RedirectView

@Controller
@RequestMapping
class OAuthWithdrawResource(
    private val oAuthService: OAuthService,
    private val oAuthFacade: OAuthFacade,
) {
    /** 회원 탈퇴용 로그인 페이지 */
    @GetMapping("/withdraw/login")
    suspend fun getWithdrawLoginPage(
        model: Model,
        request: ServerHttpRequest,
    ): String {
        val kakaoRedirectUrl = oAuthService.getOAuthWithdrawLoginLink(OAuthProvider.KAKAO, request.uri.toString())
        val googleRedirectUrl = oAuthService.getOAuthWithdrawLoginLink(OAuthProvider.GOOGLE, request.uri.toString())
        val appleRedirectUrl = oAuthService.getOAuthWithdrawLoginLink(OAuthProvider.APPLE, request.uri.toString())

        model.addAttribute("kakaoRedirectUrl", kakaoRedirectUrl.link)
        model.addAttribute("googleRedirectUrl", googleRedirectUrl.link)
        model.addAttribute("appleRedirectUrl", appleRedirectUrl.link)
        return "withdrawLogin"
    }

    /** oauth callback용 url, 로그인 완료되면 /withdraw로 redirect */
    @GetMapping("/kakao/callback")
    suspend fun getKakaoCallbackPage(
        model: Model,
        request: ServerHttpRequest,
        @RequestParam code: String,
    ): RedirectView {
        val oAuthToken = oAuthService.getOAuthWithdrawToken(OAuthProvider.KAKAO, code)
        val susuToken = oAuthFacade.login(
            provider = OAuthProvider.KAKAO,
            request = OAuthLoginRequest(oAuthToken.accessToken),
            deviceContext = UserDeviceContextImpl.getDefault()
        ).accessToken

        return RedirectView("/withdraw?xSusuAuthToken=$susuToken")
    }

    /** oauth callback용 url, 로그인 완료되면 /withdraw로 redirect */
    @GetMapping("/GOOGLE/callback")
    suspend fun getGoogleCallbackPage(
        model: Model,
        request: ServerHttpRequest,
        @RequestParam code: String,
    ): RedirectView {
        val googleAccessToken = oAuthService.getOAuthWithdrawToken(OAuthProvider.GOOGLE, code).accessToken
        val susuToken = oAuthFacade.login(
            provider = OAuthProvider.GOOGLE,
            request = OAuthLoginRequest(googleAccessToken),
            deviceContext = UserDeviceContextImpl.getDefault()
        ).accessToken

        return RedirectView("/withdraw?xSusuAuthToken=$susuToken&googleAccessToken=$googleAccessToken")
    }

    /** oauth callback용 url, 로그인 완료되면 /withdraw로 redirect */
    @GetMapping("/apple/callback")
    suspend fun getAppleCallbackPage(
        model: Model,
        request: ServerHttpRequest,
        @RequestParam code: String,
    ): RedirectView {
        val appleAccessToken = oAuthService.getOAuthWithdrawToken(OAuthProvider.APPLE, code).accessToken
        val susuToken = oAuthFacade.login(
            provider = OAuthProvider.APPLE,
            request = OAuthLoginRequest(appleAccessToken),
            deviceContext = UserDeviceContextImpl.getDefault()
        ).accessToken

        return RedirectView("/withdraw?xSusuAuthToken=$susuToken&appleAccessToken=$appleAccessToken")
    }

    /** 회원 탈퇴 페이지 */
    @GetMapping("/withdraw")
    suspend fun getWithdrawPage(
        model: Model,
        @RequestParam xSusuAuthToken: String,
        @RequestParam googleAccessToken: String?,
        @RequestParam appleAccessToken: String?,
    ): String {
        model.addAttribute("xSusuAuthToken", xSusuAuthToken)
        model.addAttribute("googleAccessToken", googleAccessToken)
        model.addAttribute("appleAccessToken", appleAccessToken)
        return "withdraw"
    }
}
