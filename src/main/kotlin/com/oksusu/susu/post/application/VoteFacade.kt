package com.oksusu.susu.post.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.common.dto.SusuPageRequest
import com.oksusu.susu.config.database.TransactionTemplates
import com.oksusu.susu.count.application.CountService
import com.oksusu.susu.count.domain.Count
import com.oksusu.susu.count.domain.vo.CountTargetType
import com.oksusu.susu.event.model.DeleteVoteCountEvent
import com.oksusu.susu.extension.coExecute
import com.oksusu.susu.extension.coExecuteOrNull
import com.oksusu.susu.post.domain.Post
import com.oksusu.susu.post.domain.VoteHistory
import com.oksusu.susu.post.domain.VoteOption
import com.oksusu.susu.post.domain.vo.PostType
import com.oksusu.susu.post.infrastructure.repository.model.GetVoteSpec
import com.oksusu.susu.post.infrastructure.repository.model.SearchVoteSpec
import com.oksusu.susu.post.model.VoteCountModel
import com.oksusu.susu.post.model.VoteOptionAndHistoryModel
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
import com.oksusu.susu.user.application.BlockService
import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.data.domain.Slice
import org.springframework.data.domain.SliceImpl
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
) {
    private val logger = KotlinLogging.logger { }

    suspend fun createVote(user: AuthUser, request: CreateVoteRequest): CreateAndUpdateVoteResponse {
        voteOptionService.validateSeq(request.options)
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
                boardModel = boardService.getBoard(request.boardId)
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

        val models = voteService.getAllVotesExceptBlock(getVoteSpec)

        val groupByPostId = models.content.groupBy { model -> model.post.id }

        val votes = models.content.map { model -> model.post }.distinct()
        val options = groupByPostId.map { group ->
            group.key to group.value.map { model -> VoteOptionModel.from(model.voteOption) }
        }.toMap()
        val counts = groupByPostId.map { group ->
            group.key to group.value.sumOf { model -> model.optionCount }
        }.toMap()

        val contents = votes.map { vote ->
            VoteAndOptionsWithCountResponse.of(
                vote = vote,
                count = counts[vote.id]!!,
                options = options[vote.id]!!,
                boardModel = boardService.getBoard(vote.boardId)
            )
        }

        return SliceImpl(contents, pageRequest.toDefault(), models.hasNext())
    }

    suspend fun getVote(user: AuthUser, id: Long): VoteAllInfoResponse {
        val (voteInfos, voteHistory) = parZip(
            { voteService.getVoteAllInfo(id) },
            { voteHistoryService.findByUidAndPostId(user.uid, id) }
        ) { voteInfos, voteHistory ->
            voteInfos to voteHistory
        }

        val vote = voteInfos[0].post
        val creator = voteInfos[0].creator
        val options = voteInfos.map { voteInfo -> voteInfo.voteOption }
        val optionCounts = voteInfos.associate { voteInfo -> voteInfo.voteOption.id to voteInfo.optionCount }

        val optionCountModels = options.map { option ->
            VoteOptionCountModel.of(
                option = option,
                count = optionCounts[option.id]!!,
                isVoted = voteHistory?.takeIf { history -> history.voteOptionId == option.id }
                    ?.let { true }
                    ?: false
            )
        }

        val voteCount = optionCounts.values.sum()

        return VoteAllInfoResponse.of(
            vote = VoteCountModel.of(
                vote,
                voteCount,
                boardService.getBoard(vote.boardId)
            ),
            options = optionCountModels,
            creator = creator,
            isMine = user.uid == creator.id
        )
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
        return parZip(
            { postService.validateAuthority(id, user.uid) },
            { voteHistoryService.validateHistoryNotExist(id) },
            { voteService.getVoteAndOptions(id) }
        ) { _, _, voteInfos ->
            val vote = voteInfos[0].post
            val options = voteInfos.map { voteInfo ->
                VoteOptionAndHistoryModel.of(option = voteInfo.voteOption, isVoted = false)
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
                boardModel = boardService.getBoard(request.boardId)
            )
        }
    }
}
