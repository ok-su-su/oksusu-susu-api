package com.oksusu.susu.auth.presentation

import com.oksusu.susu.auth.application.AuthService
import com.oksusu.susu.auth.model.AuthUser
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "Auth")
@RestController
@RequestMapping(value = ["/api"], produces = [MediaType.APPLICATION_JSON_VALUE])
class authResource(
    private val authService: AuthService
) {
    private val logger = mu.KotlinLogging.logger { }

    /** 로그아웃을 합니다 */
    @Operation(summary = "logout")
    @PostMapping("/v1/auth/logout")
    suspend fun logout(
        authUser: AuthUser) {
        return authService.logout(authUser)
    }
}
