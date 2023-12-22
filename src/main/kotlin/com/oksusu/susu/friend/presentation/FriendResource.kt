package com.oksusu.susu.friend.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapPage
import com.oksusu.susu.friend.application.FriendService
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

@Tag(name = "지인")
@RestController
@RequestMapping(value = ["/api/v1/friends"], produces = [MediaType.APPLICATION_JSON_VALUE])
class FriendResource(
    private val friendService: FriendService,
) {
    @Operation(summary = "친구 검색")
    @GetMapping
    suspend fun search(
        user: AuthUser,
        @ParameterObject searchRequest: SearchFriendRequest,
        @ParameterObject pageRequest: SusuPageRequest,
    ) = friendService.search(
        user = user,
        searchRequest = searchRequest,
        pageRequest = pageRequest
    ).wrapPage()

    @Operation(summary = "친구 생성")
    @PostMapping
    suspend fun create(
        user: AuthUser,
        @RequestBody request: CreateFriendRequest,
    ) = friendService.create(user, request).wrapCreated()
}
