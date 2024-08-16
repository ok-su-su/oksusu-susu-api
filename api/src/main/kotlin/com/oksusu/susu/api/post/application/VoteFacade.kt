package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.common.dto.SusuPageRequest
import com.oksusu.susu.api.common.lock.LockManager
import com.oksusu.susu.api.count.application.CountService
import com.oksusu.susu.api.event.model.DeleteVoteCountEvent
import com.oksusu.susu.api.post.model.*
import com.oksusu.susu.api.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.api.post.model.request.CreateVoteRequest
import com.oksusu.susu.api.post.model.request.OverwriteVoteHistoryRequest
import com.oksusu.susu.api.post.model.request.UpdateVoteRequest
import com.oksusu.susu.api.post.model.response.*
import com.oksusu.susu.api.post.model.vo.SearchVoteRequest
import com.oksusu.susu.api.user.application.BlockService
import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.domain.common.extension.coExecute
import com.oksusu.susu.domain.common.extension.coExecuteOrNull
import com.oksusu.susu.domain.config.database.TransactionTemplates
import com.oksusu.susu.domain.count.domain.Count
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteHistory
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.post.domain.vo.PostType
import com.oksusu.susu.domain.post.infrastructure.repository.model.GetVoteSpec
import com.oksusu.susu.domain.post.infrastructure.repository.model.SearchVoteSpec
import com.oksusu.susu.domain.post.infrastructure.repository.model.VoteSortType
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.slf4j.MDCContext
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
    private val lockManager: LockManager,
) {
    private val logger = KotlinLogging.logger { }

    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateAndUpdateVoteResponse {
        voteValidateService.validateCreateVoteRequest(request)
        boardService.validateExistBoard(request.boardId)

        return txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
            val createdPost = Post(
                uid = user.uid,
                type = PostType.VOTE,
                content = request.content,
                boardId = request.boardId
            ).run { postService.saveSync(this) }

            val options = request.options.map { option ->
                VoteOption(
                    postId = createdPost.id,
                    content = option.content,
                    seq = option.seq
                )
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

        val searchVoteSpec = SearchVoteSpec(
            content = searchRequest.content,
            mine = searchRequest.mine,
            sortType = searchRequest.sortType ?: VoteSortType.LATEST,
            boardId = searchRequest.boardId
        )

        val getVoteSpec = GetVoteSpec(
            uid = user.uid,
            searchSpec = searchVoteSpec,
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
        return parZipWithMDC(
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
        lockManager.lock("$id") {
            when (request.isCancel) {
                true -> cancelVote(user.uid, id, request.optionId)
                false -> castVote(user.uid, id, request.optionId)
            }
        }
    }

    private suspend fun castVote(uid: Long, postId: Long, optionId: Long) {
        val (voteCount, voteOptionCount) = parZipWithMDC(
            { voteHistoryService.validateVoteNotExist(uid, postId) },
            { voteOptionService.validateCorrespondWithVote(postId, optionId) },
            { countService.findByTargetIdAndTargetType(postId, CountTargetType.POST) },
            { countService.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION) }
        ) { _, _, voteCount, voteOptionCount -> voteCount to voteOptionCount }

        txTemplates.writer.coExecuteOrNull(Dispatchers.IO + MDCContext()) {
            VoteHistory(uid = uid, postId = postId, voteOptionId = optionId)
                .run { voteHistoryService.saveSync(this) }

            val updatedVoteCount = voteCount.apply { count++ }
            val updatedVoteOptionCount = voteOptionCount.apply { count++ }

            countService.saveAllSync(listOf(updatedVoteCount, updatedVoteOptionCount))
        }
    }

    private suspend fun cancelVote(uid: Long, postId: Long, optionId: Long) {
        val (voteCount, voteOptionCount) = parZipWithMDC(
            { voteHistoryService.validateVoteExist(uid, postId, optionId) },
            { voteOptionService.validateCorrespondWithVote(postId, optionId) },
            { countService.findByTargetIdAndTargetType(postId, CountTargetType.POST) },
            { countService.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION) }
        ) { _, _, voteCount, voteOptionCount -> voteCount to voteOptionCount }

        txTemplates.writer.coExecuteOrNull(Dispatchers.IO + MDCContext()) {
            voteHistoryService.deleteByUidAndPostId(uid, postId)

            val updatedVoteCount = voteCount.apply { count-- }
            val updatedVoteOptionCount = voteOptionCount.apply { count-- }

            countService.saveAllSync(listOf(updatedVoteCount, updatedVoteOptionCount))
        }
    }

    suspend fun overwriteVote(user: AuthUser, postId: Long, request: OverwriteVoteHistoryRequest) {
        val history = parZipWithMDC(
            { voteHistoryService.findByUidAndPostId(user.uid, postId) },
            { voteOptionService.validateCorrespondWithVote(postId, request.optionId) }
        ) { history, _ -> history }

        if (history == null) {
            /** 투표가 존재하지않을 경우 */
            parZipWithMDC(
                { countService.findByTargetIdAndTargetType(postId, CountTargetType.POST) },
                { countService.findByTargetIdAndTargetType(request.optionId, CountTargetType.VOTE_OPTION) }
            ) { voteCount, voteOptionCount ->
                VoteHistory(uid = user.uid, postId = postId, voteOptionId = request.optionId)
                    .run { voteHistoryService.saveSync(this) }

                val updatedVoteCount = voteCount.apply { count++ }
                val updatedVoteOptionCount = voteOptionCount.apply { count++ }

                txTemplates.writer.coExecuteOrNull(Dispatchers.IO + MDCContext()) {
                    countService.saveAllSync(listOf(updatedVoteCount, updatedVoteOptionCount))
                }
            }
        } else {
            /** 투표가 존재할 경우 다른 옵션으로 투표한 경우에만 동작하면 됨 */
            if (request.optionId != history.voteOptionId) {
                parZipWithMDC(
                    { countService.findByTargetIdAndTargetType(history.voteOptionId, CountTargetType.VOTE_OPTION) },
                    { countService.findByTargetIdAndTargetType(request.optionId, CountTargetType.VOTE_OPTION) }
                ) { beforeOptionCount, newOptionCount ->
                    val updatedBeforeOptionCount = beforeOptionCount.apply { count-- }
                    val updatedNewOptionCount = newOptionCount.apply { count++ }

                    txTemplates.writer.coExecuteOrNull(Dispatchers.IO + MDCContext()) {
                        history.apply { voteOptionId = request.optionId }
                            .run { voteHistoryService.saveSync(this) }

                        countService.saveAllSync(listOf(updatedBeforeOptionCount, updatedNewOptionCount))
                    }
                }
            }
        }
    }

    suspend fun deleteVote(user: AuthUser, id: Long) {
        val (vote, options) = parZipWithMDC(
            { voteService.getVote(id) },
            { voteOptionService.getVoteOptions(id) }
        ) { vote, options -> vote to options }

        if (vote.uid != user.uid) {
            throw InvalidRequestException(ErrorCode.NO_AUTHORITY_ERROR)
        }

        val optionIds = options.map { option -> option.id }

        txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
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

    suspend fun update(user: AuthUser, id: Long, request: UpdateVoteRequest): CreateAndUpdateVoteResponse {
        voteValidateService.validateUpdateVoteRequest(request)

        return parZipWithMDC(
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

            val updatedVote = txTemplates.writer.coExecute(Dispatchers.IO + MDCContext()) {
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
