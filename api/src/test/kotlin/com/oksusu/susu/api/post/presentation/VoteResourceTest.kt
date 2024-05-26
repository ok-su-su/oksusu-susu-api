package com.oksusu.susu.api.post.presentation

import com.oksusu.susu.api.ApiIntegrationSpec
import com.oksusu.susu.api.auth.model.AuthContextImpl
import com.oksusu.susu.api.auth.model.AuthUserImpl
import com.oksusu.susu.api.bulk.BulkService
import com.oksusu.susu.api.common.dto.SusuPageRequest
import com.oksusu.susu.api.post.application.VoteFacade
import com.oksusu.susu.api.post.model.BoardModel
import com.oksusu.susu.api.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.api.post.model.vo.SearchVoteRequest
import com.oksusu.susu.api.testExtension.getBodyOrThrow
import com.oksusu.susu.common.extension.equalsFromYearToSec
import com.oksusu.susu.domain.count.domain.Count
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.count.infrastructure.CountRepository
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.post.infrastructure.repository.BoardRepository
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.post.infrastructure.repository.VoteHistoryRepository
import com.oksusu.susu.domain.post.infrastructure.repository.VoteOptionRepository
import com.oksusu.susu.domain.post.infrastructure.repository.model.VoteSortType
import com.oksusu.susu.domain.user.domain.UserBlock
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.UserBlockTargetType
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import com.oksusu.susu.domain.user.infrastructure.UserBlockRepository
import com.oksusu.susu.domain.user.infrastructure.UserRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.longs.shouldBeGreaterThanOrEqual

class VoteResourceTest(
    private val voteResource: VoteResource,
    private val voteFacade: VoteFacade,
    private val postRepository: PostRepository,
    private val voteOptionRepository: VoteOptionRepository,
    private val countRepository: CountRepository,
    private val voteHistoryRepository: VoteHistoryRepository,
    private val boardRepository: BoardRepository,
    private val bulkService: BulkService,
    private val blockRepository: UserBlockRepository,
    private val userRepository: UserRepository,
) : ApiIntegrationSpec({
    val logger = KotlinLogging.logger { }

    var authUser = AuthUserImpl(
        uid = 1L,
        context = AuthContextImpl(
            name = "user",
            role = AccountRole.USER,
            profileImageUrl = null,
            userStatusTypeInfo = UserStatusTypeInfo.ACTIVE
        )
    )
    var boards: Map<Long, BoardModel> = emptyMap()
    var posts: Map<Long, Post> = emptyMap()
    var options: Map<Long, VoteOption> = emptyMap()
    var counts: Map<Long, List<Count>> = emptyMap()

    beforeSpec {
        boards = boardRepository.findAllByIsActive(true)
            .map { board -> BoardModel.from(board) }
            .associateBy { board -> board.id }

        bulkService.voteBulkInsert()

        posts = postRepository.findAll()
            .associateBy { post -> post.id }
        options = voteOptionRepository.findAll()
            .associateBy { option -> option.id }
        counts = countRepository.findAll()
            .groupBy { count -> count.targetId }

        val user = userRepository.findAll().first()
        authUser = AuthUserImpl(
            uid = user.id,
            context = AuthContextImpl(
                name = user.name,
                role = user.role,
                profileImageUrl = user.profileImageUrl,
                userStatusTypeInfo = UserStatusTypeInfo.ACTIVE
            )
        )
    }

    afterSpec {
        postRepository.deleteAll()
        voteOptionRepository.deleteAll()
        countRepository.deleteAll()
        voteHistoryRepository.deleteAll()
    }

    describe("[투표 조회] getAllVotes") {
        context("차단한 글과 유저의 게시물은") {
            beforeEach {
                blockRepository.save(
                    UserBlock(
                        uid = authUser.uid,
                        targetId = 2,
                        targetType = UserBlockTargetType.USER,
                        reason = "그냥"
                    )
                )
                blockRepository.save(
                    UserBlock(
                        uid = authUser.uid,
                        targetId = posts.values.last().id,
                        targetType = UserBlockTargetType.POST,
                        reason = "그냥"
                    )
                )
            }
            afterEach {
                blockRepository.deleteAll()
            }

            it("조회되면 안된다.") {
                val searchReq = SearchVoteRequest(
                    content = null,
                    mine = null,
                    sortType = null,
                    boardId = null
                )

                val pageReq = SusuPageRequest(
                    page = 0,
                    size = 30,
                    sort = null
                )

                val res = voteResource.searchVotes(authUser, searchReq, pageReq)

                res.data.forEach { model ->
                    model.uid shouldNotBeEqual 2
                    model.id shouldNotBeEqual posts.values.last().id
                }
            }
        }

        context("조회시") {
            it("글이 크기보다 적게 있다면, 다 조회된다.") {
                val searchReq = SearchVoteRequest(
                    content = null,
                    mine = null,
                    sortType = null,
                    boardId = null
                )

                val pageReq = SusuPageRequest(
                    page = 0,
                    size = 15,
                    sort = null
                )

                val res = voteResource.searchVotes(authUser, searchReq, pageReq)

                res.data.size shouldBeEqual 15
                res.hasNext shouldBeEqual true
            }

            it("글이 크기보다 많이 있으면, 크기만큼 조회된다.") {
                val searchReq = SearchVoteRequest(
                    content = null,
                    mine = null,
                    sortType = null,
                    boardId = null
                )

                val pageReq = SusuPageRequest(
                    page = 1,
                    size = 15,
                    sort = null
                )

                val res = voteResource.searchVotes(authUser, searchReq, pageReq)

                res.data.size shouldNotBeEqual 15
                res.hasNext shouldBeEqual false
            }
        }

        context("조건을 변경할 떄") {
            it("내 글만 보기라면, 남의 글은 보이면 안된다.") {
                val searchReq = SearchVoteRequest(
                    content = null,
                    mine = true,
                    sortType = null,
                    boardId = null
                )

                val pageReq = SusuPageRequest(
                    page = 1,
                    size = 15,
                    sort = null
                )

                val res = voteResource.searchVotes(authUser, searchReq, pageReq)

                res.data.forEach { model ->
                    model.uid shouldBeEqual authUser.uid
                }
            }

            it("인기순 정렬시, 투표수의 내림차순으로 정렬된다") {
                val searchReq = SearchVoteRequest(
                    content = null,
                    mine = null,
                    sortType = VoteSortType.POPULAR,
                    boardId = null
                )

                val pageReq = SusuPageRequest(
                    page = 1,
                    size = 15,
                    sort = null
                )

                val res = voteResource.searchVotes(authUser, searchReq, pageReq)

                val resCounts = res.data.map { model ->
                    model.count
                }

                for (i in 0..<resCounts.size - 1) {
                    resCounts[i] shouldBeGreaterThanOrEqual resCounts[i + 1]
                }
            }

            it("boardId 지정시, 해당 board에 해당하는 글만 보인다") {
                val searchReq = SearchVoteRequest(
                    content = null,
                    mine = null,
                    sortType = VoteSortType.POPULAR,
                    boardId = boards.values.first().id
                )

                val pageReq = SusuPageRequest(
                    page = 0,
                    size = 15,
                    sort = null
                )

                val res = voteResource.searchVotes(authUser, searchReq, pageReq)

                res.data.forEach { model ->
                    model.board.id shouldBeEqual boards.values.first().id
                }
            }
        }
    }

    describe("[투표 하나 조회] getVote") {
        context("조회시") {
            it("올바른 값을 반환해야한다.") {
                val post = posts.values.last()
                val postCount = counts[post.id]!!.filter { count -> count.targetType == CountTargetType.POST }.first()
                val board = boards[post.boardId]!!
                val voteOptions = options.values.filter { option -> option.postId == post.id }
                    .sortedBy { option -> option.id }

                val res = voteResource.getVote(authUser, post.id).getBodyOrThrow()

                res.id shouldBeEqual post.id
                res.isMine shouldBeEqual (authUser.uid == post.uid)
                res.board.id shouldBeEqual post.boardId
                res.board.name shouldBeEqual board.name
                res.board.seq shouldBeEqual board.seq
                res.board.isActive shouldBeEqual board.isActive
                res.content shouldBeEqual post.content
                res.count shouldBeEqual postCount.count
                res.createdAt shouldBeEqual post.createdAt
                if (res.creatorProfile.id == authUser.uid) {
                    res.creatorProfile.name shouldBeEqual authUser.context.name
                }
                res.options.sortedBy { option -> option.id }
                    .forEachIndexed { idx, option ->
                        val voteOption = voteOptions[idx]
                        val optionCount =
                            counts[voteOption.id]!!
                                .first { count -> count.targetType == CountTargetType.VOTE_OPTION }

                        option.id shouldBeEqual voteOption.id
                        option.postId shouldBeEqual post.id
                        option.content shouldBeEqual voteOption.content
                        option.seq shouldBeEqual voteOption.seq
                        option.count shouldBeEqual optionCount.count
                    }
            }

            it("투표 여부가 표시되어야 한다.") {
                val post = posts.values.last()
                val postCount = counts[post.id]!!.filter { count -> count.targetType == CountTargetType.POST }.first()
                val board = boards[post.boardId]!!
                val voteOptions = options.values.filter { option -> option.postId == post.id }
                    .sortedBy { option -> option.id }

                voteFacade.vote(
                    authUser,
                    post.id,
                    CreateVoteHistoryRequest(isCancel = false, optionId = voteOptions.first().id)
                )

                val res = voteResource.getVote(authUser, post.id).getBodyOrThrow()

                res.id shouldBeEqual post.id
                res.isMine shouldBeEqual (authUser.uid == post.uid)
                res.board.id shouldBeEqual post.boardId
                res.board.name shouldBeEqual board.name
                res.board.seq shouldBeEqual board.seq
                res.board.isActive shouldBeEqual board.isActive
                res.content shouldBeEqual post.content
                res.count shouldBeEqual postCount.count + 1
                res.createdAt shouldBeEqual post.createdAt
                if (res.creatorProfile.id == authUser.uid) {
                    res.creatorProfile.name shouldBeEqual authUser.context.name
                }
                res.options.sortedBy { option -> option.id }
                    .forEachIndexed { idx, option ->
                        val voteOption = voteOptions[idx]
                        val optionCount =
                            counts[voteOption.id]!!
                                .first { count -> count.targetType == CountTargetType.VOTE_OPTION }

                        option.id shouldBeEqual voteOption.id
                        option.postId shouldBeEqual post.id
                        option.content shouldBeEqual voteOption.content
                        option.seq shouldBeEqual voteOption.seq
                        if (idx == 0) {
                            option.isVoted shouldBeEqual true
                            option.count shouldBeEqual optionCount.count + 1
                        } else {
                            option.isVoted shouldBeEqual false
                            option.count shouldBeEqual optionCount.count
                        }
                    }

                voteFacade.vote(
                    authUser,
                    post.id,
                    CreateVoteHistoryRequest(isCancel = true, optionId = voteOptions.first().id)
                )
            }
        }
    }

    describe("[가장 인기 있는 투표 검색] getPopularVotes") {
        context("조회시") {
            it("지정한 사이즈 만큼의 투표를 검색한다.") {
                val size = 10
                val res = voteResource.getPopularVotes(authUser, size).getBodyOrThrow()

                res.size shouldBeEqual size
            }

            it("반환한 값은 인기순으로 상위에 위치는 값이어야 한다.") {
                val size = 5
                val res = voteResource.getPopularVotes(authUser, size).getBodyOrThrow()

                val sortedCounts = counts.values
                    .flatMap { it.filter { count -> count.targetType == CountTargetType.POST } }
                    .sortedByDescending { count -> count.count }

                res.forEachIndexed { idx, model ->
                    val post = posts[sortedCounts[idx].targetId]!!
                    val board = boards[post.boardId]!!

                    model.id shouldBeEqual post.id
                    model.board.id shouldBeEqual post.boardId
                    model.board.name shouldBeEqual board.name
                    model.board.seq shouldBeEqual board.seq
                    model.board.isActive shouldBeEqual board.isActive
                    model.content shouldBeEqual post.content
                    model.count shouldBeEqual sortedCounts[idx].count
                    model.isModified shouldBeEqual post.createdAt.equalsFromYearToSec(post.modifiedAt)
                }
            }
        }
    }
})
