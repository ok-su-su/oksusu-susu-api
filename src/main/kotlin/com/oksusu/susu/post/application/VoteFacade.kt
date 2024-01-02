package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToCreateException
import com.oksusu.susu.extension.executeWithContext
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteHistory
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.domain.vo.VoteOptionSummary
import com.oksusu.susu.post.domain.vo.VoteSummary
import com.oksusu.susu.post.model.VoteCountModel
import com.oksusu.susu.post.model.VoteOptionCountModel
import com.oksusu.susu.post.model.VoteOptionModel
import com.oksusu.susu.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.post.model.request.CreateVoteRequest
import com.oksusu.susu.post.model.request.UpdateVoteRequest
import com.oksusu.susu.post.model.response.CreateAndUpdateVoteResponse
import com.oksusu.susu.post.model.response.VoteAndOptionsResponse
import com.oksusu.susu.post.model.response.VoteAndOptionsWithCountResponse
import com.oksusu.susu.post.model.response.VoteWithCountResponse
import com.oksusu.susu.post.model.vo.VoteSortRequest
import com.oksusu.susu.post.model.vo.VoteSortType
import com.oksusu.susu.user.application.UserService
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class VoteFacade(
    private val txTemplates: TransactionTemplates,
    private val postService: PostService,
    private val voteService: VoteService,
    private val voteOptionService: VoteOptionService,
    private val voteSummaryService: VoteSummaryService,
    private val voteOptionSummaryService: VoteOptionSummaryService,
    private val voteHistoryService: VoteHistoryService,
    private val userService: UserService,
    private val postCategoryService: PostCategoryService,
) {
    private val logger = mu.KotlinLogging.logger { }

    companion object {
        private const val DEFAULT_POPULAR_VOTE_COUNT = 5L
    }

    @Transactional
    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateAndUpdateVoteResponse {
        voteOptionService.validateSeq(request.options)
        postCategoryService.validateExistCategory(request.postCategoryId)

        val response = txTemplates.writer.executeWithContext {
            val createdPost = Post(
                uid = user.id,
                type = PostType.VOTE,
                content = request.content,
                postCategoryId = request.postCategoryId
            ).run { postService.saveSync(this) }

            val optionModels = request.options.map { option ->
                VoteOption.of(option, createdPost.id)
            }.run { voteOptionService.saveAllSync(this) }
                .map { option -> VoteOptionModel.from(option) }

            CreateAndUpdateVoteResponse.of(
                createdPost,
                optionModels,
                postCategoryService.getCategory(request.postCategoryId)
            )
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_POST_ERROR)

        val voteSummary = VoteSummary(postId = response.id)
        val voteOptionSummaries = response.options.map { option ->
            VoteOptionSummary(voteOptionId = option.id!!)
        }

        parZip(
            { voteSummaryService.save(voteSummary) },
            { voteOptionSummaryService.saveAll(voteOptionSummaries) }
        ) { _, _ -> }

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

        return parZip(
            { postCategoryService.getCategoryByIdIn(votes.content.map { vote -> vote.postCategoryId }) },
            { voteOptionService.getOptionsByPostIdIn(votes.content.map { vote -> vote.id }) }
        ) { postCategoryModels, options ->
            val optionModels = options.map { VoteOptionModel.from(it) }

            votes.map { vote ->
                VoteAndOptionsResponse.of(
                    vote,
                    optionModels.filter { it.postId == vote.id },
                    postCategoryModels.first { model -> vote.postCategoryId == model.id }
                )
            }
        }
    }

    private suspend fun getLatestVotes(
        sortRequest: VoteSortRequest,
        uid: Long,
        pageRequest: SusuPageRequest,
    ): Slice<Post> {
        return voteService.getAllVotes(
            sortRequest.mine,
            uid,
            sortRequest.categoryId,
            pageRequest.toDefault()
        )
    }

    private suspend fun getPopularVotes(
        sortRequest: VoteSortRequest,
        uid: Long,
        pageRequest: SusuPageRequest,
    ): Slice<Post> {
        val from = pageRequest.page!! * pageRequest.size!!
        val to = from + pageRequest.size
        val summaries = voteSummaryService.getSummaryBetween(from, to)

        return voteService.getAllVotesOrderByPopular(
            sortRequest.mine,
            uid,
            sortRequest.categoryId,
            summaries.map { it.postId },
            pageRequest.toDefault()
        )
    }

    @Transactional(readOnly = true)
    suspend fun getVote(user: AuthUser, id: Long): VoteAndOptionsWithCountResponse {
        val voteInfos = voteService.getVoteAndOptions(id)

        val vote = voteInfos[0].post
        val options = voteInfos.map { voteInfo -> voteInfo.voteOption }

        return parZip(
            { userService.findByIdOrThrow(vote.uid) },
            { voteSummaryService.getSummaryByPostId(id) },
            { voteOptionSummaryService.getSummariesByOptionIdIn(options.map { option -> option.id }) },
            { postCategoryService.getCategory(vote.postCategoryId) }
        ) { creator, voteSummary, optionSummaries, postCategoryModel ->
            val optionCountModels = options.map { option ->
                VoteOptionCountModel.of(option, optionSummaries.first { it.voteOptionId == option.id })
            }

            VoteAndOptionsWithCountResponse.of(
                vote = VoteCountModel.of(
                    vote,
                    voteSummary,
                    postCategoryModel
                ),
                options = optionCountModels,
                creator = creator,
                isMine = user.id == creator.id
            )
        }
    }

    @Transactional
    suspend fun vote(user: AuthUser, id: Long, request: CreateVoteHistoryRequest) {
        when (request.isCancel) {
            true -> cancelVote(user.id, id, request.optionId)
            false -> castVote(user.id, id, request.optionId)
        }
    }

    private suspend fun castVote(uid: Long, postId: Long, optionId: Long) {
        voteHistoryService.validateVoteNotExist(uid, postId)

        parZip(
            { voteSummaryService.increaseCount(postId) },
            { voteOptionSummaryService.increaseCount(optionId) }
        ) { _, _ -> }

        txTemplates.writer.executeWithContext {
            VoteHistory(uid = uid, postId = postId, voteOptionId = optionId)
                .run { voteHistoryService.saveSync(this) }
        }
    }

    private suspend fun cancelVote(uid: Long, postId: Long, optionId: Long) {
        voteHistoryService.validateVoteExist(uid, postId, optionId)

        parZip(
            { voteSummaryService.decreaseCount(postId) },
            { voteOptionSummaryService.decreaseCount(optionId) }
        ) { _, _ -> }

        txTemplates.writer.executeWithContext {
            voteHistoryService.deleteByUidAndPostId(uid, postId)
        }
    }

    @Transactional
    suspend fun deleteVote(user: AuthUser, id: Long) {
        parZip(
            { voteSummaryService.deleteSummaryByPostId(id) },
            { voteOptionService.getVoteOptions(id) }
        ) { _, options ->
            voteOptionSummaryService.deleteSummaryByOptionIdIn(options.map { it.id })
        }

        voteService.softDeleteVote(user.id, id)
    }

    @Transactional(readOnly = true)
    suspend fun getPopularVotes(user: AuthUser): List<VoteWithCountResponse> {
        val summaries = voteSummaryService.getPopularVotes(DEFAULT_POPULAR_VOTE_COUNT)
        val votes = voteService.getAllVotesByIdIn(summaries.map { summary -> summary.postId })
        val postCategoryModels = postCategoryService.getCategoryByIdIn(votes.map { vote -> vote.postCategoryId })

        return summaries.map { summary ->
            val vote = votes.first { vote -> vote.id == summary.postId }

            VoteWithCountResponse.of(
                post = vote,
                summary = summary,
                postCategoryModel = postCategoryModels.first { it.id == vote.postCategoryId }
            )
        }
    }

    @Transactional
    suspend fun update(user: AuthUser, id: Long, request: UpdateVoteRequest): CreateAndUpdateVoteResponse {
        val updatedPostCategory = parZip(
            // 투표 된게 있는지 검사
            { voteHistoryService.validateHistoryNotExist(id) },
            { postCategoryService.getCategory(request.postCategoryId) }
        ) { _, updatedPostCategory ->
            updatedPostCategory
        }

        val voteInfos = voteService.getVoteAndOptions(id)

        val vote = voteInfos[0].post
        val options = voteInfos.map { voteInfo -> VoteOptionModel.from(voteInfo.voteOption) }

        val updatedVote = txTemplates.writer.executeWithContext {
            vote.apply {
                content = request.content
                postCategoryId = request.postCategoryId
            }.run { postService.saveSync(this) }
        } ?: throw FailToCreateException(ErrorCode.FAIL_TO_CREATE_POST_ERROR)

        return CreateAndUpdateVoteResponse.of(
            updatedVote,
            options,
            updatedPostCategory
        )
    }
}
