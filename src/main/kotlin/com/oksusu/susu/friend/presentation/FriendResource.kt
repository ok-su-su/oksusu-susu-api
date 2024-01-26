package com.oksusu.susu.friend.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapPage
import com.oksusu.susu.friend.application.FriendFacade
import com.oksusu.susu.friend.model.request.CreateFriendRequest
import com.oksusu.susu.friend.model.request.SearchFriendRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.FRIEND_SWAGGER_TAG, description = "친구 관리 API")
@RestController
@RequestMapping(value = ["/api/v1/friends"], produces = [MediaType.APPLICATION_JSON_VALUE])
class FriendResource(
    private val friendFacade: FriendFacade,
) {
    /**
     * **검색조건**
     * - name, phoneNumber 검색 조건은 현재 nullable
     *
     * **검색 정렬 조건**
     * - createdAt (생성)
     */
    @Operation(summary = "친구 검색")
    @GetMapping
    suspend fun search(
        user: AuthUser,
        @ParameterObject searchRequest: SearchFriendRequest,
        @ParameterObject pageRequest: SusuPageRequest,
    ) = friendFacade.search(
        user = user,
        searchRequest = searchRequest,
        pageRequest = pageRequest
    ).wrapPage()

    @Operation(summary = "친구 생성")
    @PostMapping
    suspend fun create(
        user: AuthUser,
        @RequestBody request: CreateFriendRequest,
    ) = friendFacade.create(user, request).wrapCreated()
}
