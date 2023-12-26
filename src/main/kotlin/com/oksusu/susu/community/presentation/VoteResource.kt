package com.oksusu.susu.community.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.community.application.VoteFacade
import com.oksusu.susu.community.domain.vo.CommunityCategory
import com.oksusu.susu.community.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.community.model.request.CreateVoteRequest
import com.oksusu.susu.community.model.vo.VoteSortType
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.extension.wrapSlice
import com.oksusu.susu.extension.wrapVoid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "투표")
@RestController
@RequestMapping(value = ["/api/v1/votes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VoteResource (
    private val voteFacade: VoteFacade,
){
    @Operation(summary = "투표 생성")
    @PostMapping
    suspend fun createVote(
        user: AuthUser,
        @RequestBody request: CreateVoteRequest,
    ) = voteFacade.createVote(user, request).wrapCreated()

    @Operation(summary = "투표 검색")
    @GetMapping
    suspend fun getAllVotes(
        user: AuthUser,
        @RequestParam sortType: VoteSortType = VoteSortType.LATEST,
        @RequestParam isMine: Boolean = false,
        @RequestParam category: CommunityCategory = CommunityCategory.ALL,
        @ParameterObject sliceRequest: SusuPageRequest,
    ) = voteFacade.getAllVotes(user, sortType, isMine, category, sliceRequest).wrapSlice()

    @Operation(summary = "투표 하나 검색")
    @GetMapping("/{id}")
    suspend fun getVote(
        user: AuthUser,
        @PathVariable id: Long,
    ) = voteFacade.getVote(user, id).wrapOk()

    @Operation(summary = "투표하기")
    @PostMapping("/{id}")
    suspend fun getAllVotes(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: CreateVoteHistoryRequest,
    ) = voteFacade.vote(user, id, request).wrapCreated()

    @Operation(summary = "투표 삭제하기")
    @DeleteMapping("/{id}")
    suspend fun deleteVote(
        user: AuthUser,
        @PathVariable id: Long,
    ) = voteFacade.deleteVote(user, id).wrapVoid()

    @Operation(summary = "가장 인기 있는 투표 검색")
    @GetMapping("/popular")
    suspend fun getPopularVotes(
        user: AuthUser,
    ) = voteFacade.getPopularVotes(user).wrapOk()
}
