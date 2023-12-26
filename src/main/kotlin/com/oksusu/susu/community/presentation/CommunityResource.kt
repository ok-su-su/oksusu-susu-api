package com.oksusu.susu.community.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.application.CommunityFacade
import com.oksusu.susu.community.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.community.model.request.CreateVoteRequest
import com.oksusu.susu.extension.*
import com.oksusu.susu.friend.model.request.CreateFriendRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "커뮤니티")
@RestController
@RequestMapping(value = ["/api/v1/communities"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CommunityResource (
    private val communityFacade: CommunityFacade,
){
    @Operation(summary = "투표 생성")
    @PostMapping("/votes")
    suspend fun createVote(
        user: AuthUser,
        @RequestBody request: CreateVoteRequest,
    ) = communityFacade.createVote(user, request).wrapCreated()

    @Operation(summary = "투표 검색")
    @GetMapping("/votes")
    suspend fun getAllVotes(
        user: AuthUser,
        @ParameterObject sliceRequest: SusuSliceRequest,
    ) = communityFacade.getAllVotes(user, sliceRequest).wrapSlice()

    @Operation(summary = "투표 하나 검색")
    @GetMapping("/votes/{id}")
    suspend fun getVote(
        user: AuthUser,
        @PathVariable id: Long,
    ) = communityFacade.getVote(user, id).wrapOk()

    @Operation(summary = "투표하기")
    @PostMapping("/votes/{id}")
    suspend fun getAllVotes(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: CreateVoteHistoryRequest,
    ) = communityFacade.vote(user, id, request).wrapCreated()

    @Operation(summary = "투표 삭제하기")
    @DeleteMapping("/votes/{id}")
    suspend fun deleteVote(
        user: AuthUser,
        @PathVariable id: Long,
    ) = communityFacade.deleteVote(user, id).wrapVoid()

    @Operation(summary = "가장 인기 있는 투표 검색")
    @GetMapping("/votes/popular")
    suspend fun getPopularVotes(
        user: AuthUser,
    ) = communityFacade.getPopularVotes(user).wrapOk()
}
