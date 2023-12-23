package com.oksusu.susu.community.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.VoteOption
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.model.VoteOptionModel
import com.oksusu.susu.community.model.request.CreateVoteRequest
import com.oksusu.susu.community.model.response.CreateVoteResponse
import com.oksusu.susu.community.model.response.VoteResponse
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CommunityFacade(
    private val txTemplates: TransactionTemplates,
    private val communityService: CommunityService,
    private val voteOptionService: VoteOptionService,
) {
    @Transactional
    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateVoteResponse {
        voteOptionService.validateSeq(request.options)

        return txTemplates.writer.executeWithContext {
            val createdCommunity = Community(
                uid = user.id,
                type = CommunityType.VOTE,
                category = request.category,
                content = request.content,
            ).run { communityService.saveSync(this) }

            val optionModels = request.options.map { option ->
                VoteOption.of(option, createdCommunity.id)
            }.run { voteOptionService.saveAllSync(this) }
                .map { option -> VoteOptionModel.from(option) }

            CreateVoteResponse.of(createdCommunity, optionModels)
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_COMMUNITY_ERROR)
    }

    @Transactional(readOnly = true)
    suspend fun getAllVotes(user: AuthUser, sliceRequest: SusuSliceRequest): Slice<VoteResponse> {
        val votes = communityService.getAllVotes(sliceRequest)
        val voteIds = votes.content.map { it.id }
        val voteOptions = voteOptionService.getOptionsByCommunityIdIn(voteIds).map {
            VoteOptionModel.from(it)
        }

        return votes.map { vote ->
            VoteResponse(
                id = vote.id,
                uid = vote.uid,
                category = vote.category,
                content = vote.content,
                options = voteOptions.filter { it.communityId == vote.id }
            )
        }
    }
}