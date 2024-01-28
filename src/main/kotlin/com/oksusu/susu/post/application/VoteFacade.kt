package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.block.application.BlockService
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.count.application.CountService
import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.count.domain.vo.CountTargetType
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.extension.coExecuteOrNull
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteHistory
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.GetAllVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.SearchVoteSpec
import com.oksusu.susu.post.model.VoteCountModel
import com.oksusu.susu.post.model.VoteOptionCountModel
import com.oksusu.susu.post.model.VoteOptionModel
import com.oksusu.susu.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.post.model.request.CreateVoteRequest
import com.oksusu.susu.post.model.request.UpdateVoteRequest
import com.oksusu.susu.post.model.response.CreateAndUpdateVoteResponse
import com.oksusu.susu.post.model.response.VoteAllInfoResponse
import com.oksusu.susu.post.model.response.VoteAndOptionsWithCountResponse
import com.oksusu.susu.post.model.response.VoteWithCountResponse
import com.oksusu.susu.post.model.vo.SearchVoteRequest
import com.oksusu.susu.user.application.UserService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class VoteFacade(
    private val txTemplates: TransactionTemplates,
    private val postService: PostService,
    private val voteService: VoteService,
    private val voteOptionService: VoteOptionService,
    private val voteHistoryService: VoteHistoryService,
    private val userService: UserService,
    private val postCategoryService: PostCategoryService,
    private val blockService: BlockService,
    private val countService: CountService,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateAndUpdateVoteResponse {
        voteOptionService.validateSeq(request.options)
        postCategoryService.validateExistCategory(request.postCategoryId)

        return txTemplates.writer.coExecute {
            val createdPost = Post(
                uid = user.uid,
                type = PostType.VOTE,
                content = request.content,
                postCategoryId = request.postCategoryId
            ).run { postService.saveSync(this) }

            val options = request.options.map { option ->
                VoteOption.of(option, createdPost.id)
            }.run { voteOptionService.saveAllSync(this) }

            val voteCount = Count.toVoteLike(createdPost)
            val voteOptionCounts = options.map { option -> Count.toVoteOptionLike(option) }

            countService.saveAllSync(voteOptionCounts.plus(voteCount))

            CreateAndUpdateVoteResponse.of(
                uid = user.uid,
                post = createdPost,
                optionModels = options.map { option -> VoteOptionModel.from(option) },
                postCategoryModel = postCategoryService.getCategory(request.postCategoryId)
            )
        }
    }

    suspend fun getAllVotes(
        user: AuthUser,
        searchRequest: SearchVoteRequest,
        pageRequest: SusuPageRequest,
    ): Slice<VoteAndOptionsWithCountResponse> {
        val searchSpec = SearchVoteSpec.from(searchRequest)

        val userAndPostBlockIdModel = blockService.getUserAndPostBlockTargetIds(user.uid)

        val getAllVoteSpec = GetAllVoteSpec(
            uid = user.uid,
            searchSpec = searchSpec,
            userBlockIds = userAndPostBlockIdModel.userBlockIds,
            postBlockIds = userAndPostBlockIdModel.postBlockIds,
            pageable = pageRequest.toDefault()
        )

        val voteAndCountModels = voteService.getAllVotesExceptBlock(getAllVoteSpec)
        val optionModels = voteOptionService.getOptionsByPostIdIn(
            voteAndCountModels.content.map { model -> model.post.id }
        )
            .map { VoteOptionModel.from(it) }

        return voteAndCountModels.map { vote ->
            VoteAndOptionsWithCountResponse.of(
                vote = vote.post,
                count = vote.count,
                options = optionModels.filter { option -> option.postId == vote.post.id },
                postCategoryModel = postCategoryService.getCategory(vote.post.postCategoryId)
            )
        }
    }

    suspend fun getVote(user: AuthUser, id: Long): VoteAllInfoResponse {
        val voteInfos = voteService.getVoteAndOptions(id)

        val vote = voteInfos[0].post
        val options = voteInfos.map { voteInfo -> voteInfo.voteOption }
        val optionIds = options.map { option -> option.id }

        return parZip(
            { userService.findByIdOrThrow(vote.uid) },
            { countService.findByTargetIdAndTargetType(vote.id, CountTargetType.POST) },
            { countService.findAllByTargetTypeAndTargetIdIn(optionIds, CountTargetType.VOTE_OPTION) },
            { postCategoryService.getCategory(vote.postCategoryId) }
        ) { creator, voteCount, optionCount, postCategoryModel ->
            val optionCountModels = options.map { option ->
                VoteOptionCountModel.of(option, optionCount.first { it.targetId == option.id })
            }

            VoteAllInfoResponse.of(
                vote = VoteCountModel.of(
                    vote,
                    voteCount,
                    postCategoryModel
                ),
                options = optionCountModels,
                creator = creator,
                isMine = user.uid == creator.id
            )
        }
    }

    suspend fun vote(user: AuthUser, id: Long, request: CreateVoteHistoryRequest) {
        when (request.isCancel) {
            true -> cancelVote(user.uid, id, request.optionId)
            false -> castVote(user.uid, id, request.optionId)
        }
    }

    private suspend fun castVote(uid: Long, postId: Long, optionId: Long) {
        voteHistoryService.validateVoteNotExist(uid, postId)

        val (voteCount, voteOptionCount) = parZip(
            { countService.findByTargetIdAndTargetType(postId, CountTargetType.POST) },
            { countService.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION) }
        ) { voteCount, voteOptionCount -> voteCount to voteOptionCount }

        txTemplates.writer.coExecute {
            VoteHistory(uid = uid, postId = postId, voteOptionId = optionId)
                .run { voteHistoryService.saveSync(this) }

            val updatedVoteCount = voteCount.apply { count++ }
            val updatedVoteOptionCount = voteOptionCount.apply { count++ }

            countService.saveAllSync(listOf(updatedVoteCount, updatedVoteOptionCount))
        }
    }

    private suspend fun cancelVote(uid: Long, postId: Long, optionId: Long) {
        voteHistoryService.validateVoteExist(uid, postId, optionId)

        val (voteCount, voteOptionCount) = parZip(
            { countService.findByTargetIdAndTargetType(postId, CountTargetType.POST) },
            { countService.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION) }
        ) { voteCount, voteOptionCount -> voteCount to voteOptionCount }

        txTemplates.writer.coExecuteOrNull {
            voteHistoryService.deleteByUidAndPostId(uid, postId)

            val updatedVoteCount = voteCount.apply { count-- }
            val updatedVoteOptionCount = voteOptionCount.apply { count-- }

            countService.saveAllSync(listOf(updatedVoteCount, updatedVoteOptionCount))
        }
    }

    suspend fun deleteVote(user: AuthUser, id: Long) {
        postService.validateAuthority(id, user.uid)

        val (vote, options) = parZip(
            { voteService.getVote(id) },
            { voteOptionService.getVoteOptions(id) }
        ) { vote, options -> vote to options }

        val optionIds = options.map { option -> option.id }

        txTemplates.writer.coExecute {
            vote.apply { isActive = false }.run {
                postService.saveSync(this)
            }

            countService.deleteByTargetIdAndTargetType(vote.id, CountTargetType.POST)

            countService.deleteAllByTargetTypeAndTargetIdIn(CountTargetType.VOTE_OPTION, optionIds)
        }
    }

    suspend fun getPopularVotes(user: AuthUser, size: Int): List<VoteWithCountResponse> {
        val userAndPostBlockIdModel = blockService.getUserAndPostBlockTargetIds(user.uid)

        val voteAndCountModels = voteService.getPopularVotesExceptBlock(
            uid = user.uid,
            userBlockIds = userAndPostBlockIdModel.userBlockIds,
            postBlockIds = userAndPostBlockIdModel.postBlockIds,
            size = size
        )

        return voteAndCountModels.map { model ->
            VoteWithCountResponse.of(
                model = model,
                postCategoryModel = postCategoryService.getCategory(model.post.postCategoryId)
            )
        }
    }

    suspend fun update(user: AuthUser, id: Long, request: UpdateVoteRequest): CreateAndUpdateVoteResponse {
        return parZip(
            { postService.validateAuthority(id, user.uid) },
            { voteHistoryService.validateHistoryNotExist(id) },
            { postCategoryService.getCategory(request.postCategoryId) },
            { voteService.getVoteAndOptions(id) }
        ) { _, _, updatedPostCategory, voteInfos ->
            val vote = voteInfos[0].post
            val options = voteInfos.map { voteInfo -> VoteOptionModel.from(voteInfo.voteOption) }

            val updatedVote = txTemplates.writer.coExecute {
                vote.apply {
                    content = request.content
                    postCategoryId = request.postCategoryId
                }.run { postService.saveSync(this) }
            }

            CreateAndUpdateVoteResponse.of(
                uid = user.uid,
                post = updatedVote,
                optionModels = options,
                postCategoryModel = updatedPostCategory
            )
        }
    }
}
