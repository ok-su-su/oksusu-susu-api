package com.oksusu.susu.api.auth.presentation

import com.oksusu.susu.api.auth.application.AuthFacade
import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.auth.model.response.TokenRefreshRequest
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.extension.wrapOk
import com.oksusu.susu.api.extension.wrapVoid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.AUTH_SWAGGER_TAG, description = "Auth API")
@RestController
@RequestMapping(value = ["/api/v1/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthResource(
    private val authFacade: AuthFacade,
) {
    /** 로그아웃을 합니다 */
    @Operation(summary = "logout")
    @PostMapping("/logout")
    suspend fun logout(
        authUser: AuthUser,
    ) = authFacade.logout(authUser).wrapVoid()

    /** 토큰 재발급 */
    @Operation(summary = "token refresh")
    @PostMapping("/token/refresh")
    suspend fun tokenRefresh(
        @RequestBody request: TokenRefreshRequest,
    ) = authFacade.refreshToken(request).wrapOk()

    /**
     *  회원 탈퇴
     *  kakao는 아무것도 안넘겨줘도 됩니다
     *  google 회원탈퇴시 구글측 accessToken을 param으로 넘겨줘야함
     */
    @Operation(summary = "withdraw")
    @PostMapping("/withdraw")
    suspend fun tokenRefresh(
        authUser: AuthUser,
        @RequestParam code: String?,
        @RequestParam accessToken: String?,
    ) = authFacade.withdraw(authUser, code, accessToken).wrapVoid()
}
