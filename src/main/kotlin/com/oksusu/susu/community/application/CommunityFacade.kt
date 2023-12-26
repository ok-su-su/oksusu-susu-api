package com.oksusu.susu.community.application

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuSliceRequest
import com.oksusu.susu.community.domain.*
import com.oksusu.susu.community.domain.vo.CommunityType
import com.oksusu.susu.community.domain.vo.VoteOptionSummary
import com.oksusu.susu.community.domain.vo.VoteSummary
import com.oksusu.susu.community.model.VoteCountModel
import com.oksusu.susu.community.model.VoteOptionCountModel
import com.oksusu.susu.community.model.VoteOptionModel
import com.oksusu.susu.community.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.community.model.request.CreateVoteRequest
import com.oksusu.susu.community.model.response.CreateVoteResponse
import com.oksusu.susu.community.model.response.VoteAndOptionsWithCountResponse
import com.oksusu.susu.community.model.response.VoteAndOptionsResponse
import com.oksusu.susu.community.model.response.VoteWithCountResponse
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.user.application.UserService
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
    private val voteHistoryService: VoteHistoryService,
    private val userService: UserService,
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
    suspend fun getAllVotes(user: AuthUser, sliceRequest: SusuSliceRequest): Slice<VoteAndOptionsResponse> {
        val votes = voteService.getAllVotes(sliceRequest)
        val voteIds = votes.content.map { it.id }
        val options = voteOptionService.getOptionsByCommunityIdIn(voteIds).map {
            VoteOptionModel.from(it)
        }

        return votes.map { vote ->
            VoteAndOptionsResponse.of(vote, options.filter { it.communityId == vote.id })
        }
    }

    @Transactional(readOnly = true)
    suspend fun getVote(user: AuthUser, id: Long): VoteAndOptionsWithCountResponse {
        val voteInfos = voteService.getVoteAndOptions(id)
        val vote = voteInfos[0].community
        val options = voteInfos.map { it.voteOption }

        val creator = userService.findByIdOrThrow(vote.uid)

        val voteSummary = voteSummaryService.getSummaryByCommunityId(id)
        val optionSummaries = voteOptionSummaryService.getSummariesByOptionIdIn(options.map { it.id })

        val optionCountModels = options.map { option ->
            VoteOptionCountModel.of(option, optionSummaries.first { it.voteOptionId == option.id })
        }

        return VoteAndOptionsWithCountResponse.of(
            VoteCountModel.of(vote, voteSummary),
            optionCountModels,
            creator,
            user.id == creator.id
        )
    }

    @Transactional
    suspend fun vote(user: AuthUser, id: Long, request: CreateVoteHistoryRequest) {
        when (request.isCancel) {
            true -> cancelVote(user.id, id, request.optionId)
            false -> castVote(user.id, id, request.optionId)
        }
    }

    private suspend fun castVote(uid: Long, communityId: Long, optionId: Long) {
        voteHistoryService.validateVoteNotExist(uid, communityId)

        txTemplates.writer.executeWithContext {
            VoteHistory(uid = uid, communityId = communityId, voteOptionId = optionId)
                .run { voteHistoryService.saveSync(this) }
        }

        voteSummaryService.increaseCount(communityId)
        voteOptionSummaryService.increaseCount(optionId)
    }

    private suspend fun cancelVote(uid: Long, communityId: Long, optionId: Long) {
        voteHistoryService.validateVoteExist(uid, communityId, optionId)

        txTemplates.writer.executeWithContext {
            voteHistoryService.deleteByUidAndCommunityId(uid, communityId)
        }

        voteSummaryService.decreaseCount(communityId)
        voteOptionSummaryService.decreaseCount(optionId)
    }

    @Transactional
    suspend fun deleteVote(user: AuthUser, id: Long) {
        val softDeletedVote = voteService.softDeleteVote(user.id, id)

        txTemplates.writer.executeWithContext {
            communityService.saveSync(softDeletedVote)
        }
    }

    @Transactional(readOnly = true)
    suspend fun getPopularVotes(user: AuthUser): List<VoteWithCountResponse> {
        val summaries = voteSummaryService.getPopularVotes()
        val communityIds = summaries.map { it.communityId }
        val votes = voteService.getAllVotesByIdIn(communityIds)

        return summaries.map { summary ->
            VoteWithCountResponse.of(votes.first { it.id == summary.communityId }, summary)
        }
    }
}
