package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.AuthService
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.extension.wrapVoid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth")
@RestController
@RequestMapping(value = ["/api/v1/auth"], produces = [MediaType.APPLICATION_JSON_VALUE])
class AuthResource(
    private val authService: AuthService
) {
    private val logger = mu.KotlinLogging.logger { }

    /** 로그아웃을 합니다 */
    @Operation(summary = "logout")
    @PostMapping("/logout")
    suspend fun logout(
        authUser: AuthUser,
    ) = authService.logout(authUser).wrapVoid()

    /** 토큰 재발급 */
    @Operation(summary = "token refresh")
    @PostMapping("/token/refresh")
    suspend fun tokenRefresh(
        authUser: AuthUser,
        @RequestParam refreshToken: String,
    ) = authService.refreshToken(authUser, refreshToken).wrapOk()
}
