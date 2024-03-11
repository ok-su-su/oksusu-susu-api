package com.oksusu.susu.api.user.presentation

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.extension.wrapOk
import com.oksusu.susu.api.user.application.UserFacade
import com.oksusu.susu.api.user.model.request.UpdateUserInfoRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.USER_SWAGGER_TAG, description = "유저 API")
@RestController
@RequestMapping(value = ["/api/v1/users"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserResource(
    private val userFacade: UserFacade,
) {
    /** token 기반으로 유저 정보를 조회 */
    @Operation(summary = "유저 정보 조회")
    @GetMapping("/my-info")
    suspend fun getUserInfo(
        user: AuthUser,
    ) = userFacade.getUserInfo(user).wrapOk()

    @Operation(summary = "유저 정보 수정")
    @PatchMapping("/{uid}")
    suspend fun updateUserInfo(
        user: AuthUser,
        @PathVariable uid: Long,
        @RequestBody request: UpdateUserInfoRequest,
    ) = userFacade.updateUserInfo(uid, user, request).wrapOk()
}
