package com.oksusu.susu.community.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.VoteOption
import com.oksusu.susu.community.domain.VoteOptionSummary
import com.oksusu.susu.community.domain.VoteSummary
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.model.VoteCountModel
import com.oksusu.susu.community.model.VoteOptionCountModel
import com.oksusu.susu.community.model.VoteOptionModel
import com.oksusu.susu.community.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.community.model.request.CreateVoteRequest
import com.oksusu.susu.community.model.response.CreateVoteResponse
import com.oksusu.susu.community.model.response.VoteCountResponse
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
    private val voteService: VoteService,
    private val voteOptionService: VoteOptionService,
    private val voteSummaryService: VoteSummaryService,
    private val voteOptionSummaryService: VoteOptionSummaryService,
) {
    @Transactional
    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateVoteResponse {
        voteOptionService.validateSeq(request.options)


        val response = txTemplates.writer.executeWithContext {
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

        VoteSummary(communityId = response.id).run {
            voteSummaryService.save(this)
        }

        response.options.map { option ->
            VoteOptionSummary(voteOptionId = option.id!!)
        }.run {
            voteOptionSummaryService.saveAll(this)
        }

        return response
    }

    @Transactional(readOnly = true)
    suspend fun getAllVotes(user: AuthUser, sliceRequest: SusuSliceRequest): Slice<VoteResponse> {
        val votes = voteService.getAllVotes(sliceRequest)
        val voteIds = votes.content.map { it.id }
        val options = voteOptionService.getOptionsByCommunityIdIn(voteIds).map {
            VoteOptionModel.from(it)
        }

        return votes.map { vote ->
            VoteResponse.of(vote, options.filter { it.communityId == vote.id })
        }
    }

    @Transactional(readOnly = true)
    suspend fun getVote(user: AuthUser, id: Long): VoteCountResponse {
        val vote = voteService.getVote(id)
        val voteSummary = voteSummaryService.getSummaryByCommunityId(id)
        val options = voteOptionService.getVoteOptions(vote.id)
        val optionSummaries = voteOptionSummaryService.getSummariesByOptionIdIn(options.map { it.id })
        val optionCountModels = options.map { option->
            VoteOptionCountModel.of(option, optionSummaries.first { it.voteOptionId == option.id })
        }

        return VoteCountResponse.of(VoteCountModel.of(vote,voteSummary), optionCountModels)
    }

    fun createVoteHistory(user: AuthUser, id: Long, request: CreateVoteHistoryRequest) {

    }
}