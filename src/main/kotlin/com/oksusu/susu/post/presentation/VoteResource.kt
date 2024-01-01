package com.oksusu.susu.post.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.extension.wrapCreated
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.extension.wrapSlice
import com.oksusu.susu.extension.wrapVoid
import com.oksusu.susu.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.post.model.request.CreateVoteRequest
import com.oksusu.susu.post.model.request.UpdateVoteRequest
import com.oksusu.susu.post.model.vo.VoteSortRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = "투표")
@RestController
@RequestMapping(value = ["/api/v1/votes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VoteResource(
    private val voteFacade: com.oksusu.susu.post.application.VoteFacade,
) {
    @Operation(summary = "투표 생성")
    @PostMapping
    suspend fun createVote(
        user: AuthUser,
        @RequestBody request: CreateVoteRequest,
    ) = voteFacade.createVote(user, request).wrapCreated()

    /** 카테고리 전체 검색 : 0 */
    @Operation(summary = "투표 조회")
    @GetMapping
    suspend fun getAllVotes(
        user: AuthUser,
        @ParameterObject sortRequest: VoteSortRequest,
        @ParameterObject sliceRequest: SusuPageRequest,
    ) = voteFacade.getAllVotes(user, sortRequest, sliceRequest).wrapSlice()

    @Operation(summary = "투표 하나 조회")
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

    @Operation(summary = "투표 업데이트")
    @PatchMapping("/{id}")
    suspend fun update(
        user: AuthUser,
        @PathVariable id: Long,
        @RequestBody request: UpdateVoteRequest,
    ) = voteFacade.update(user, id, request).wrapOk()

    /** 총 투표 수가 0 일 경우 제외됩니다  */
    @Operation(summary = "가장 인기 있는 투표 검색")
    @GetMapping("/popular")
    suspend fun getPopularVotes(
        user: AuthUser,
    ) = voteFacade.getPopularVotes(user).wrapOk()
}
