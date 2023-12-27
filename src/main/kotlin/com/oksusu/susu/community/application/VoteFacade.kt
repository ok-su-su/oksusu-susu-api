package com.oksusu.susu.community.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryAssignmentService
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.category.domain.CategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.category.infrastructure.CategoryAssignmentRepository
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.common.util.Quad
import com.oksusu.susu.community.domain.*
import com.oksusu.susu.community.domain.vo.CommunityCategory
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
import com.oksusu.susu.community.model.vo.VoteSortRequest
import com.oksusu.susu.community.model.vo.VoteSortType
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.user.application.UserService
import kotlinx.coroutines.Dispatchers
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.math.log

@Service
class VoteFacade(
    private val txTemplates: TransactionTemplates,
    private val communityService: CommunityService,
    private val voteService: VoteService,
    private val voteOptionService: VoteOptionService,
    private val voteSummaryService: VoteSummaryService,
    private val voteOptionSummaryService: VoteOptionSummaryService,
    private val voteHistoryService: VoteHistoryService,
    private val userService: UserService,
    private val categoryAssignmentService: CategoryAssignmentService,
    private val categoryService: CategoryService,
) {
    val logger = mu.KotlinLogging.logger { }

    @Transactional
    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateVoteResponse {
        voteOptionService.validateSeq(request.options)

        val response = txTemplates.writer.executeWithContext {
            val createdCommunity = Community(
                uid = user.id,
                type = CommunityType.VOTE,
                content = request.content,
            ).run { communityService.saveSync(this) }

            val optionModels = request.options.map { option ->
                VoteOption.of(option, createdCommunity.id)
            }.run { voteOptionService.saveAllSync(this) }
                .map { option -> VoteOptionModel.from(option) }

            CategoryAssignment(
                targetId = createdCommunity.id,
                targetType = CategoryAssignmentType.COMMUNITY,
                categoryId = request.categoryId,
            ).run { categoryAssignmentService.saveSync(this) }

            CreateVoteResponse.of(createdCommunity, optionModels, categoryService.getCategory(request.categoryId))
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_COMMUNITY_ERROR)

        parZip(Dispatchers.IO,
            {
                VoteSummary(communityId = response.id).run {
                    voteSummaryService.save(this)
                }
            },
            {
                response.options.map { option ->
                    VoteOptionSummary(voteOptionId = option.id!!)
                }.run {
                    voteOptionSummaryService.saveAll(this)
                }
            }, { _, _ -> })

        return response
    }

    @Transactional(readOnly = true)
    suspend fun getAllVotes(
        user: AuthUser,
        sortRequest: VoteSortRequest,
        pageRequest: SusuPageRequest,
    ): Slice<VoteAndOptionsResponse> {
        val votes = when (sortRequest.sortType) {
            VoteSortType.LATEST -> getLatestVotes(sortRequest, user.id, pageRequest)
            VoteSortType.POPULAR -> getPopularVotes(sortRequest, user.id, pageRequest)
        }
        val voteIds = votes.content.map { it.id }

        val (categoryAssignments, options) = parZip(Dispatchers.IO,
            { categoryAssignmentService.findAllByTypeAndIdIn(CategoryAssignmentType.COMMUNITY, voteIds) },
            { voteOptionService.getOptionsByCommunityIdIn(voteIds) },
            { a, b -> a to b })

        val optionModels = options.map { VoteOptionModel.from(it) }

        return votes.map { vote ->
            VoteAndOptionsResponse.of(
                vote,
                optionModels.filter { it.communityId == vote.id },
                categoryService.getCategory(categoryAssignments.first { it.targetId == vote.id }.categoryId)
            )
        }
    }

    private suspend fun getLatestVotes(
        sortRequest: VoteSortRequest,
        uid: Long,
        pageRequest: SusuPageRequest,
    ): Slice<Community> {
        return voteService.getAllVotes(
            sortRequest.mine,
            uid,
            sortRequest.category,
            pageRequest.toDefault()
        )
    }

    private suspend fun getPopularVotes(
        sortRequest: VoteSortRequest,
        uid: Long,
        pageRequest: SusuPageRequest,
    ): Slice<Community> {
        val from = pageRequest.page!! * pageRequest.size!!
        val to = from + pageRequest.size
        val summaries = voteSummaryService.getSummaryBetween(from, to)

        return voteService.getAllVotesOrderByPopular(
            sortRequest.mine,
            uid,
            sortRequest.category,
            summaries.map { it.communityId },
            pageRequest.toDefault()
        )
    }

    @Transactional(readOnly = true)
    suspend fun getVote(user: AuthUser, id: Long): VoteAndOptionsWithCountResponse {
        val voteInfos = voteService.getVoteAndOptions(id)
        val vote = voteInfos[0].community
        val options = voteInfos.map { it.voteOption }
        val (creator, voteSummary, optionSummaries, categoryAssignment) = parZip(Dispatchers.IO,
            { userService.findByIdOrThrow(vote.uid) },
            { voteSummaryService.getSummaryByCommunityId(id) },
            { voteOptionSummaryService.getSummariesByOptionIdIn(options.map { it.id }) },
            { categoryAssignmentService.findByIdAndTypeOrThrow(vote.id, CategoryAssignmentType.COMMUNITY) },
            { a, b, c, d -> Quad(a, b, c, d) }
        )

        val optionCountModels = options.map { option ->
            VoteOptionCountModel.of(option, optionSummaries.first { it.voteOptionId == option.id })
        }

        return VoteAndOptionsWithCountResponse.of(
            VoteCountModel.of(vote, voteSummary, categoryService.getCategory(categoryAssignment.categoryId)),
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

        parZip(Dispatchers.IO,
            { voteSummaryService.increaseCount(communityId) },
            { voteOptionSummaryService.increaseCount(optionId) },
            { _, _ -> }
        )

        txTemplates.writer.executeWithContext {
            VoteHistory(uid = uid, communityId = communityId, voteOptionId = optionId)
                .run { voteHistoryService.saveSync(this) }
        }
    }

    private suspend fun cancelVote(uid: Long, communityId: Long, optionId: Long) {
        voteHistoryService.validateVoteExist(uid, communityId, optionId)

        parZip(Dispatchers.IO,
            { voteSummaryService.decreaseCount(communityId) },
            { voteOptionSummaryService.decreaseCount(optionId) },
            { _, _ -> }
        )

        txTemplates.writer.executeWithContext {
            voteHistoryService.deleteByUidAndCommunityId(uid, communityId)
        }
    }

    @Transactional
    suspend fun deleteVote(user: AuthUser, id: Long) {
        val options = parZip(Dispatchers.IO,
            { voteSummaryService.deleteSummaryByCommunityId(id) },
            { voteOptionService.getVoteOptions(id) },
            { _, b -> b })
        voteOptionSummaryService.deleteSummaryByOptionIdIn(options.map { id })

        voteService.softDeleteVote(user.id, id)
    }

    @Transactional(readOnly = true)
    suspend fun getPopularVotes(user: AuthUser): List<VoteWithCountResponse> {
        val summaries = voteSummaryService.getPopularVotes(5)
        val votes = voteService.getAllVotesByIdIn(summaries.map { it.communityId })
        val categoryAssignments =
            categoryAssignmentService.findAllByTypeAndIdIn(CategoryAssignmentType.COMMUNITY, votes.map { it.id })

        return summaries.map { summary ->
            VoteWithCountResponse.of(
                votes.first { it.id == summary.communityId },
                summary,
                categoryService.getCategory(categoryAssignments.first { it.targetId == summary.communityId }.categoryId)
            )
        }
    }
}
