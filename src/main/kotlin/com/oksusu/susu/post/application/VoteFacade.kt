package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.SusuConfig
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.count.application.CountService
import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.count.domain.vo.CountTargetType
import com.oksusu.susu.event.model.DeleteVoteCountEvent
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.extension.coExecuteOrNull
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteHistory
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.GetVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.SearchVoteSpec
import com.oksusu.susu.post.model.OnboardingVoteOptionCountModel
import com.oksusu.susu.post.model.VoteCountModel
import com.oksusu.susu.post.model.VoteOptionAndHistoryModel
import com.oksusu.susu.post.model.VoteOptionCountModel
import com.oksusu.susu.post.model.VoteOptionModel
import com.oksusu.susu.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.post.model.request.CreateVoteRequest
import com.oksusu.susu.post.model.request.UpdateVoteRequest
import com.oksusu.susu.post.model.response.CreateAndUpdateVoteResponse
import com.oksusu.susu.post.model.response.OnboardingVoteResponse
import com.oksusu.susu.post.model.response.VoteAllInfoResponse
import com.oksusu.susu.post.model.response.VoteAndOptionsWithCountResponse
import com.oksusu.susu.post.model.response.VoteWithCountResponse
import com.oksusu.susu.post.model.vo.SearchVoteRequest
import com.oksusu.susu.user.application.BlockService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Slice
import org.springframework.stereotype.Service

@Service
class VoteFacade(
    private val txTemplates: TransactionTemplates,
    private val postService: PostService,
    private val voteService: VoteService,
    private val voteOptionService: VoteOptionService,
    private val voteHistoryService: VoteHistoryService,
    private val boardService: BoardService,
    private val blockService: BlockService,
    private val countService: CountService,
    private val eventPublisher: ApplicationEventPublisher,
    private val onboardingGetVoteConfig: SusuConfig.OnboardingGetVoteConfig,
    private val voteValidateService: VoteValidateService,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateAndUpdateVoteResponse {
        voteValidateService.validateCreateVoteRequest(request)
        boardService.validateExistBoard(request.boardId)

        return txTemplates.writer.coExecute {
            val createdPost = Post(
                uid = user.uid,
                type = PostType.VOTE,
                content = request.content,
                boardId = request.boardId
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
                optionModels = options.map { option ->
                    VoteOptionAndHistoryModel.of(option = option, isVoted = false)
                },
                boardModel = boardService.getBoard(request.boardId),
                isMine = true
            )
        }
    }

    suspend fun getAllVotes(
        user: AuthUser,
        searchRequest: SearchVoteRequest,
        pageRequest: SusuPageRequest,
    ): Slice<VoteAndOptionsWithCountResponse> {
        val userAndPostBlockIdModel = blockService.getUserAndPostBlockTargetIds(user.uid)

        val getVoteSpec = GetVoteSpec(
            uid = user.uid,
            searchSpec = SearchVoteSpec.from(searchRequest),
            userBlockIds = userAndPostBlockIdModel.userBlockIds,
            postBlockIds = userAndPostBlockIdModel.postBlockIds,
            pageable = pageRequest.toDefault()
        )

        val voteAndCountModels = voteService.getVoteAndCountExceptBlock(getVoteSpec)

        val options = voteOptionService.getOptionsByPostIdIn(
            voteAndCountModels.content.map { model -> model.post.id }
        ).map { VoteOptionModel.from(it) }.groupBy { model -> model.postId }

        return voteAndCountModels.map { model ->
            VoteAndOptionsWithCountResponse.of(
                vote = model.post,
                count = model.voteCount,
                options = options[model.post.id]!!,
                boardModel = boardService.getBoard(model.post.boardId),
                isMine = user.uid == model.post.uid
            )
        }
    }

    suspend fun getVote(user: AuthUser, voteId: Long): VoteAllInfoResponse {
        return parZip(
            { voteService.getVoteAndCreator(voteId) },
            { voteOptionService.getOptionAndCount(voteId) },
            { voteHistoryService.findByUidAndPostId(user.uid, voteId) }
        ) { voteAndCreatorModel, optionAndCountModels, voteHistory ->
            val optionCountModels = optionAndCountModels.map { model ->
                VoteOptionCountModel.of(
                    option = model.voteOption,
                    count = model.count,
                    isVoted = voteHistory?.takeIf { history -> history.voteOptionId == model.voteOption.id }
                        ?.let { true }
                        ?: false
                )
            }

            val voteCount = optionAndCountModels.sumOf { model -> model.count }

            VoteAllInfoResponse.of(
                vote = VoteCountModel.of(
                    voteAndCreatorModel.post,
                    voteCount,
                    boardService.getBoard(voteAndCreatorModel.post.boardId)
                ),
                options = optionCountModels,
                creator = voteAndCreatorModel.user,
                isMine = user.uid == voteAndCreatorModel.user.id
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
        val (voteCount, voteOptionCount) = parZip(
            { voteHistoryService.validateVoteNotExist(uid, postId) },
            { countService.findByTargetIdAndTargetType(postId, CountTargetType.POST) },
            { countService.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION) }
        ) { _, voteCount, voteOptionCount -> voteCount to voteOptionCount }

        txTemplates.writer.coExecuteOrNull() {
            VoteHistory(uid = uid, postId = postId, voteOptionId = optionId)
                .run { voteHistoryService.saveSync(this) }

            val updatedVoteCount = voteCount.apply { count++ }
            val updatedVoteOptionCount = voteOptionCount.apply { count++ }

            countService.saveAllSync(listOf(updatedVoteCount, updatedVoteOptionCount))
        }
    }

    private suspend fun cancelVote(uid: Long, postId: Long, optionId: Long) {
        val (voteCount, voteOptionCount) = parZip(
            { voteHistoryService.validateVoteExist(uid, postId, optionId) },
            { countService.findByTargetIdAndTargetType(postId, CountTargetType.POST) },
            { countService.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION) }
        ) { _, voteCount, voteOptionCount -> voteCount to voteOptionCount }

        txTemplates.writer.coExecuteOrNull {
            voteHistoryService.deleteByUidAndPostId(uid, postId)

            val updatedVoteCount = voteCount.apply { count-- }
            val updatedVoteOptionCount = voteOptionCount.apply { count-- }

            countService.saveAllSync(listOf(updatedVoteCount, updatedVoteOptionCount))
        }
    }

    suspend fun deleteVote(user: AuthUser, id: Long) {
        val (vote, options) = parZip(
            { voteService.getVote(id) },
            { voteOptionService.getVoteOptions(id) }
        ) { vote, options -> vote to options }

        if (vote.uid != user.uid) {
            throw InvalidRequestException(ErrorCode.NO_AUTHORITY_ERROR)
        }

        val optionIds = options.map { option -> option.id }

        txTemplates.writer.coExecute {
            vote.apply { isActive = false }.run {
                postService.saveSync(this)
            }

            eventPublisher.publishEvent(
                DeleteVoteCountEvent(
                    postId = vote.id,
                    optionIds = optionIds
                )
            )
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
                boardModel = boardService.getBoard(model.post.boardId)
            )
        }
    }

    /** 투표가 진행된 경우 업데이트 불가능 */
    suspend fun update(user: AuthUser, id: Long, request: UpdateVoteRequest): CreateAndUpdateVoteResponse {
        voteValidateService.validateUpdateVoteRequest(request)

        return parZip(
            { voteHistoryService.findByUidAndPostId(user.uid, id) },
            { voteService.getVoteAndOptions(id) }
        ) { voteHistory, voteInfos ->
            val vote = voteInfos[0].post
            val options = voteInfos.map { voteInfo ->
                val option = voteInfo.voteOption

                VoteOptionAndHistoryModel.of(
                    option = option,
                    isVoted = voteHistory?.takeIf { history -> history.voteOptionId == option.id }
                        ?.let { true }
                        ?: false
                )
            }

            if (vote.uid != user.uid) {
                throw InvalidRequestException(ErrorCode.NO_AUTHORITY_ERROR)
            }

            val updatedVote = txTemplates.writer.coExecute {
                vote.apply {
                    content = request.content
                    boardId = request.boardId
                }.run { postService.saveSync(this) }
            }

            CreateAndUpdateVoteResponse.of(
                uid = user.uid,
                post = updatedVote,
                optionModels = options,
                boardModel = boardService.getBoard(request.boardId),
                isMine = true
            )
        }
    }

    suspend fun getOnboardingVote(): OnboardingVoteResponse {
        val optionCountModels = voteOptionService.getOptionAndCount(onboardingGetVoteConfig.voteId)
            .map { model ->
                OnboardingVoteOptionCountModel.of(
                    option = model.voteOption,
                    count = model.count
                )
            }

        return OnboardingVoteResponse(
            options = optionCountModels
        )
    }
}
