package com.oksusu.susu.api.friend.presentation

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.common.extension.wrapCreated
import com.oksusu.susu.common.extension.wrapOk
import com.oksusu.susu.common.extension.wrapPage
import com.oksusu.susu.common.extension.wrapVoid
import com.oksusu.susu.api.friend.application.FriendFacade
import com.oksusu.susu.api.friend.model.request.CreateAndUpdateFriendRequest
import com.oksusu.susu.api.friend.model.request.SearchFriendRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
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
        @RequestBody request: CreateAndUpdateFriendRequest,
    ) = friendFacade.create(user, request).wrapCreated()

    @Operation(summary = "친구 수정")
    @PutMapping("/{id}")
    suspend fun update(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: CreateAndUpdateFriendRequest,
    ) = friendFacade.update(
        user = user,
        id = id,
        request = request
    ).wrapOk()

    @Operation(summary = "친구 삭제")
    @DeleteMapping
    suspend fun delete(
        user: AuthUser,
        @RequestParam ids: Set<Long>,
    ) = friendFacade.delete(user, ids).wrapVoid()
}
