package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.extension.coExecuteOrNull
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteHistory
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.domain.vo.VoteOptionSummary
import com.oksusu.susu.post.domain.vo.VoteSummary
import com.oksusu.susu.post.infrastructure.repository.model.SearchVoteSpec
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
import com.oksusu.susu.post.model.vo.SearchVoteRequest
import com.oksusu.susu.post.model.vo.VoteSortType
import com.oksusu.susu.user.application.UserService
import kotlinx.coroutines.*
import org.springframework.data.domain.Pageable
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

        val response = txTemplates.writer.coExecute {
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
                post = createdPost,
                optionModels = optionModels,
                postCategoryModel = postCategoryService.getCategory(request.postCategoryId)
            )
        }

        val voteSummary = VoteSummary(postId = response.id)
        val voteOptionSummaries = response.options.map { option ->
            VoteOptionSummary(voteOptionId = option.id!!)
        }

        CoroutineScope(Dispatchers.IO).launch {
            val saveSummary = async { voteSummaryService.save(voteSummary) }
            val saveOptionSummary = async { voteOptionSummaryService.saveAll(voteOptionSummaries) }

            awaitAll(saveSummary, saveOptionSummary)
        }

        return response
    }

    @Transactional(readOnly = true)
    suspend fun getAllVotes(
        user: AuthUser,
        searchRequest: SearchVoteRequest,
        pageRequest: SusuPageRequest,
    ): Slice<VoteAndOptionsResponse> {
        val searchSpec = SearchVoteSpec.from(searchRequest)

        val votes = when (searchSpec.sortType) {
            VoteSortType.LATEST -> getLatestVotes(user.id, searchSpec, pageRequest.toDefault())
            VoteSortType.POPULAR -> getPopularVotes(user.id, searchSpec, pageRequest.toDefault())
        }

        return parZip(
            { postCategoryService.getCategoryByIdIn(votes.content.map { vote -> vote.postCategoryId }) },
            { voteOptionService.getOptionsByPostIdIn(votes.content.map { vote -> vote.id }) }
        ) { postCategoryModels, options ->
            val optionModels = options.map { VoteOptionModel.from(it) }

            votes.map { vote ->
                VoteAndOptionsResponse.of(
                    vote = vote,
                    options = optionModels.filter { it.postId == vote.id },
                    postCategoryModel = postCategoryModels.first { model -> vote.postCategoryId == model.id }
                )
            }
        }
    }

    private suspend fun getLatestVotes(
        uid: Long,
        searchSpec: SearchVoteSpec,
        pageRequest: Pageable,
    ): Slice<Post> {
        return voteService.getAllVotes(
            uid = uid,
            searchSpec = searchSpec,
            pageable = pageRequest
        )
    }

    private suspend fun getPopularVotes(
        uid: Long,
        searchSpec: SearchVoteSpec,
        pageRequest: Pageable,
    ): Slice<Post> {
        val from = pageRequest.pageNumber * pageRequest.pageSize
        val to = from + pageRequest.pageSize
        val summaries = voteSummaryService.getSummaryBetween(from, to)

        return voteService.getAllVotesOrderByPopular(
            uid = uid,
            searchSpec = searchSpec,
            ids = summaries.map { it.postId },
            pageable = pageRequest
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

        CoroutineScope(Dispatchers.IO).launch {
            val increaseSummary = async { voteSummaryService.increaseCount(postId) }
            val increaseOptionSummary = async { voteOptionSummaryService.increaseCount(optionId) }

            awaitAll(increaseSummary, increaseOptionSummary)
        }

        txTemplates.writer.coExecute {
            VoteHistory(uid = uid, postId = postId, voteOptionId = optionId)
                .run { voteHistoryService.saveSync(this) }
        }
    }

    private suspend fun cancelVote(uid: Long, postId: Long, optionId: Long) {
        voteHistoryService.validateVoteExist(uid, postId, optionId)

        CoroutineScope(Dispatchers.IO).launch {
            val decreaseSummary = async { voteSummaryService.decreaseCount(postId) }
            val decreaseOptionSummary = async { voteOptionSummaryService.decreaseCount(optionId) }

            awaitAll(decreaseSummary, decreaseOptionSummary)
        }

        txTemplates.writer.coExecuteOrNull {
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
        return parZip(
            // 투표 된게 있는지 검사
            { voteHistoryService.validateHistoryNotExist(id) },
            { postCategoryService.getCategory(request.postCategoryId) },
            { voteService.getVoteAndOptions(id) }
        ) { _, updatedPostCategory, voteInfos ->
            val vote = voteInfos[0].post
            val options = voteInfos.map { voteInfo -> VoteOptionModel.from(voteInfo.voteOption) }

            val updatedVote = txTemplates.writer.coExecute {
                vote.apply {
                    content = request.content
                    postCategoryId = request.postCategoryId
                }.run { postService.saveSync(this) }
            }

            CreateAndUpdateVoteResponse.of(
                post = updatedVote,
                optionModels = options,
                postCategoryModel = updatedPostCategory
            )
        }
    }
}
