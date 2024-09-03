package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.ApiIntegrationSpec
import com.oksusu.susu.api.auth.model.AuthContextImpl
import com.oksusu.susu.api.auth.model.AuthUserImpl
import com.oksusu.susu.api.post.model.BoardModel
import com.oksusu.susu.api.post.model.OnboardingVoteOptionCountModel
import com.oksusu.susu.api.post.model.VoteOptionWithoutIdModel
import com.oksusu.susu.api.post.model.request.CreateVoteHistoryRequest
import com.oksusu.susu.api.post.model.request.CreateVoteRequest
import com.oksusu.susu.api.post.model.request.UpdateVoteRequest
import com.oksusu.susu.api.testExtension.executeConcurrency
import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.common.exception.SusuException
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.count.domain.vo.CountType
import com.oksusu.susu.domain.count.infrastructure.CountRepository
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.post.domain.vo.PostType
import com.oksusu.susu.domain.post.infrastructure.repository.BoardRepository
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.post.infrastructure.repository.VoteHistoryRepository
import com.oksusu.susu.domain.post.infrastructure.repository.VoteOptionRepository
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import org.springframework.data.repository.findByIdOrNull
import java.util.concurrent.atomic.AtomicLong

class VoteFacadeTest(
    private val voteFacade: VoteFacade,
    private val postConfig: SusuConfig.PostConfig,
    private val onboardingGetVoteConfig: SusuConfig.OnboardingGetVoteConfig,
    private val boardRepository: BoardRepository,
    private val postRepository: PostRepository,
    private val voteOptionRepository: VoteOptionRepository,
    private val countRepository: CountRepository,
    private val voteHistoryRepository: VoteHistoryRepository,
) : ApiIntegrationSpec({
    val logger = KotlinLogging.logger { }

    val authUser = AuthUserImpl(
        uid = 1L,
        context = AuthContextImpl(
            name = "user",
            role = AccountRole.USER,
            profileImageUrl = null,
            userStatusTypeInfo = UserStatusTypeInfo.ACTIVE
        )
    )
    var boards = emptyList<BoardModel>()

    beforeContainer {
        boards = boardRepository.findAllByIsActive(true)
            .map { board -> BoardModel.from(board) }
            .associateBy { board -> board.id }.values
            .toList()
            .sortedBy { board -> board.id }
    }

    afterSpec {
        postRepository.deleteAll()
        voteOptionRepository.deleteAll()
        countRepository.deleteAll()
        voteHistoryRepository.deleteAll()
    }

    describe("[온보드 페이지용 투표 값 조회] getOnboardingVote") {
        context("조회시") {
            it("미리 지정된 option의 count를 가져온다.") {
                val models = voteOptionRepository.getOptionAndCount(onboardingGetVoteConfig.voteId)
                    .map { model ->
                        OnboardingVoteOptionCountModel.of(
                            option = model.voteOption,
                            count = model.count
                        )
                    }

                val res = voteFacade.getOnboardingVote().options

                for (i in models.indices) {
                    res[i].content shouldBeEqual models[i].content
                    res[i].count shouldBeEqual models[i].count
                }
            }
        }
    }

    describe("[투표 생성] createVote") {
        context("request가") {
            val validOptions = listOf(
                VoteOptionWithoutIdModel(seq = 1, content = "1"),
                VoteOptionWithoutIdModel(seq = 2, content = "2")
            )

            it("content 길이가 0이면 에러") {
                val req = CreateVoteRequest(
                    content = "",
                    options = validOptions,
                    boardId = boards[0].id
                )
                shouldThrow<InvalidRequestException> { voteFacade.createVote(authUser, req) }
            }

            it("content 길이가 1이면 통과") {
                val req = CreateVoteRequest(
                    content = "1",
                    options = validOptions,
                    boardId = boards[0].id
                )
                voteFacade.createVote(authUser, req)
            }

            it("content 길이가 ${postConfig.createForm.maxContentLength}이면 통과") {
                var content = ""
                for (i: Int in 1..postConfig.createForm.maxContentLength) {
                    content += "1"
                }

                val req = CreateVoteRequest(
                    content = content,
                    options = validOptions,
                    boardId = boards[0].id
                )
                voteFacade.createVote(authUser, req)
            }

            it("content 길이가 ${postConfig.createForm.maxContentLength} 초과면 에러") {
                var content = ""
                for (i: Int in 1..postConfig.createForm.maxContentLength) {
                    content += "1"
                }
                content += "2"

                val req = CreateVoteRequest(
                    content = content,
                    options = validOptions,
                    boardId = boards[0].id
                )
                shouldThrow<InvalidRequestException> { voteFacade.createVote(authUser, req) }
            }

            it("option 개수가 0이면 에러") {
                val req = CreateVoteRequest(
                    content = "0",
                    options = emptyList(),
                    boardId = boards[0].id
                )
                shouldThrow<InvalidRequestException> { voteFacade.createVote(authUser, req) }
            }

            it("option 개수가 1 이상이면 통과") {
                val req = CreateVoteRequest(
                    content = "0",
                    options = validOptions,
                    boardId = boards[0].id
                )
                voteFacade.createVote(authUser, req)
            }

            it("option content 길이가 option 개수가0이면 에러") {
                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = ""),
                    VoteOptionWithoutIdModel(seq = 2, content = "")
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options,
                    boardId = boards[0].id
                )

                shouldThrow<InvalidRequestException> { voteFacade.createVote(authUser, req) }
            }

            it("option content 길이가 1이면 통과") {
                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = "1"),
                    VoteOptionWithoutIdModel(seq = 2, content = "2")
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options,
                    boardId = boards[0].id
                )

                voteFacade.createVote(authUser, req)
            }

            it("option content 길이가 ${postConfig.createVoteOptionForm.maxContentLength}이면 통과") {
                var content = ""
                for (i: Int in 1..postConfig.createVoteOptionForm.maxContentLength) {
                    content += "1"
                }

                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = content),
                    VoteOptionWithoutIdModel(seq = 2, content = content)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options,
                    boardId = boards[0].id
                )

                voteFacade.createVote(authUser, req)
            }

            it("option content 길이가 ${postConfig.createVoteOptionForm.maxContentLength} 초과면 에러") {
                var content = ""
                for (i: Int in 1..postConfig.createVoteOptionForm.maxContentLength) {
                    content += "1"
                }
                content += "2"

                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = content),
                    VoteOptionWithoutIdModel(seq = 2, content = content)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options,
                    boardId = boards[0].id
                )

                shouldThrow<InvalidRequestException> { voteFacade.createVote(authUser, req) }
            }

            it("seq가 중복되면 에러") {
                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = "1"),
                    VoteOptionWithoutIdModel(seq = 1, content = "2")
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options,
                    boardId = boards[0].id
                )

                shouldThrow<InvalidRequestException> { voteFacade.createVote(authUser, req) }
            }

            it("seq가 올바르면 통과") {
                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = "1"),
                    VoteOptionWithoutIdModel(seq = 2, content = "2")
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options,
                    boardId = boards[0].id
                )

                voteFacade.createVote(authUser, req)
            }

            it("없는 boardId를 요청하면 에러") {

                val req = CreateVoteRequest(
                    content = "1",
                    options = validOptions,
                    boardId = boards.last().id + 1
                )
                shouldThrow<NotFoundException> { voteFacade.createVote(authUser, req) }
            }

            it("정상이면 통과") {
                val req = CreateVoteRequest(
                    content = "1",
                    options = validOptions,
                    boardId = boards[0].id
                )
                voteFacade.createVote(authUser, req)
            }
        }

        context("정상적인 request일 때,") {
            beforeEach {
                postRepository.deleteAll()
                voteOptionRepository.deleteAll()
                countRepository.deleteAll()
            }

            it("post는 1개만 생성되고, options 개수와 생성된 vote option 수는 같아야한다.") {
                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = "1"),
                    VoteOptionWithoutIdModel(seq = 2, content = "2")
                )

                val req = CreateVoteRequest(
                    content = "1",
                    options = options,
                    boardId = boards[0].id
                )

                val res = voteFacade.createVote(authUser, req)

                /** response 검증 */
                res.content shouldBeEqual req.content
                res.uid shouldBeEqual authUser.uid
                res.board.id shouldBeEqual req.boardId
                res.isModified shouldBeEqual false
                res.isMine shouldBeEqual true
                res.options.size shouldBeEqual options.size
                res.options[0].seq shouldBeEqual options[0].seq
                res.options[0].content shouldBeEqual options[0].content
                res.options[0].postId shouldBeEqual res.id
                res.options[1].seq shouldBeEqual options[1].seq
                res.options[1].content shouldBeEqual options[1].content
                res.options[1].postId shouldBeEqual res.id

                /** DB input 검증 */
                val posts = postRepository.findAll()
                posts.size shouldBeEqual 1
                posts[0].content shouldBeEqual req.content
                posts[0].uid shouldBeEqual authUser.uid
                posts[0].boardId shouldBeEqual req.boardId
                posts[0].type shouldBeEqual PostType.VOTE
                posts[0].isActive shouldBeEqual true

                val voteOptions = voteOptionRepository.findAll()
                voteOptions.size shouldBeEqual options.size
                voteOptions[0].seq shouldBeEqual options[0].seq
                voteOptions[0].content shouldBeEqual options[0].content
                voteOptions[0].postId shouldBeEqual posts[0].id
                voteOptions[1].seq shouldBeEqual options[1].seq
                voteOptions[1].content shouldBeEqual options[1].content
                voteOptions[1].postId shouldBeEqual posts[0].id
            }

            it("count는 option 개수 + 1개 생성되고 각 post와 option을 가르켜야한다.") {
                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = "11"),
                    VoteOptionWithoutIdModel(seq = 2, content = "22"),
                    VoteOptionWithoutIdModel(seq = 3, content = "33")
                )

                val req = CreateVoteRequest(
                    content = "11",
                    options = options,
                    boardId = boards[1].id
                )

                val res = voteFacade.createVote(authUser, req)

                /** response 검증 */
                res.content shouldBeEqual req.content
                res.uid shouldBeEqual authUser.uid
                res.board.id shouldBeEqual req.boardId
                res.isModified shouldBeEqual false
                res.isMine shouldBeEqual true
                res.options.size shouldBeEqual options.size
                res.options[0].seq shouldBeEqual options[0].seq
                res.options[0].content shouldBeEqual options[0].content
                res.options[0].postId shouldBeEqual res.id
                res.options[1].seq shouldBeEqual options[1].seq
                res.options[1].content shouldBeEqual options[1].content
                res.options[1].postId shouldBeEqual res.id
                res.options[2].seq shouldBeEqual options[2].seq
                res.options[2].content shouldBeEqual options[2].content
                res.options[2].postId shouldBeEqual res.id

                /** DB input 검증 */
                val postIds = postRepository.findAll().map { post -> post.id }.toList()
                val voteOptionIds = voteOptionRepository.findAll().map { option -> option.id }.toList()
                val counts = countRepository.findAll()

                val postCounts = counts.filter { count -> count.targetType == CountTargetType.POST }
                val optionCounts = counts.filter { count -> count.targetType == CountTargetType.VOTE_OPTION }

                postCounts.size shouldBeEqual 1
                postCounts[0].count shouldBeEqual 0
                postCounts[0].countType shouldBeEqual CountType.VOTE
                postCounts[0].targetId shouldBeEqual postIds[0]

                optionCounts.size shouldBeEqual voteOptionIds.size
                optionCounts[0].count shouldBeEqual 0
                optionCounts[0].countType shouldBeEqual CountType.VOTE
                optionCounts[0].targetId shouldBeEqual voteOptionIds[0]
                optionCounts[1].count shouldBeEqual 0
                optionCounts[1].countType shouldBeEqual CountType.VOTE
                optionCounts[1].targetId shouldBeEqual voteOptionIds[1]
                optionCounts[2].count shouldBeEqual 0
                optionCounts[2].countType shouldBeEqual CountType.VOTE
                optionCounts[2].targetId shouldBeEqual voteOptionIds[2]
            }

            it("DB에 정상적으로 데이터가 입력되어야한다.") {
                val options = listOf(
                    VoteOptionWithoutIdModel(seq = 1, content = "11"),
                    VoteOptionWithoutIdModel(seq = 2, content = "22"),
                    VoteOptionWithoutIdModel(seq = 3, content = "33"),
                    VoteOptionWithoutIdModel(seq = 4, content = "44")
                )

                val req = CreateVoteRequest(
                    content = "111",
                    options = options,
                    boardId = boards[1].id
                )

                val res = voteFacade.createVote(authUser, req)

                /** response 검증 */
                res.content shouldBeEqual req.content
                res.uid shouldBeEqual authUser.uid
                res.board.id shouldBeEqual req.boardId
                res.isModified shouldBeEqual false
                res.isMine shouldBeEqual true
                res.options.size shouldBeEqual options.size
                res.options[0].seq shouldBeEqual options[0].seq
                res.options[0].content shouldBeEqual options[0].content
                res.options[0].postId shouldBeEqual res.id
                res.options[1].seq shouldBeEqual options[1].seq
                res.options[1].content shouldBeEqual options[1].content
                res.options[1].postId shouldBeEqual res.id
                res.options[2].seq shouldBeEqual options[2].seq
                res.options[2].content shouldBeEqual options[2].content
                res.options[2].postId shouldBeEqual res.id
                res.options[3].seq shouldBeEqual options[3].seq
                res.options[3].content shouldBeEqual options[3].content
                res.options[3].postId shouldBeEqual res.id

                /** DB input 검증 */
                val posts = postRepository.findAll()
                posts.size shouldBeEqual 1
                posts[0].content shouldBeEqual req.content
                posts[0].uid shouldBeEqual authUser.uid
                posts[0].boardId shouldBeEqual req.boardId
                posts[0].type shouldBeEqual PostType.VOTE
                posts[0].isActive shouldBeEqual true

                val voteOptions = voteOptionRepository.findAll()
                voteOptions.size shouldBeEqual options.size
                voteOptions[0].seq shouldBeEqual options[0].seq
                voteOptions[0].content shouldBeEqual options[0].content
                voteOptions[0].postId shouldBeEqual posts[0].id
                voteOptions[1].seq shouldBeEqual options[1].seq
                voteOptions[1].content shouldBeEqual options[1].content
                voteOptions[1].postId shouldBeEqual posts[0].id
                voteOptions[2].seq shouldBeEqual options[2].seq
                voteOptions[2].content shouldBeEqual options[2].content
                voteOptions[2].postId shouldBeEqual posts[0].id
                voteOptions[3].seq shouldBeEqual options[3].seq
                voteOptions[3].content shouldBeEqual options[3].content
                voteOptions[3].postId shouldBeEqual posts[0].id

                val postIds = posts.map { post -> post.id }.toList()
                val voteOptionIds = voteOptions.map { option -> option.id }.toList()
                val counts = countRepository.findAll()

                val postCounts = counts.filter { count -> count.targetType == CountTargetType.POST }
                val optionCounts = counts.filter { count -> count.targetType == CountTargetType.VOTE_OPTION }

                postCounts.size shouldBeEqual 1
                postCounts[0].count shouldBeEqual 0
                postCounts[0].countType shouldBeEqual CountType.VOTE
                postCounts[0].targetId shouldBeEqual postIds[0]

                optionCounts.size shouldBeEqual voteOptionIds.size
                optionCounts[0].count shouldBeEqual 0
                optionCounts[0].countType shouldBeEqual CountType.VOTE
                optionCounts[0].targetId shouldBeEqual voteOptionIds[0]
                optionCounts[1].count shouldBeEqual 0
                optionCounts[1].countType shouldBeEqual CountType.VOTE
                optionCounts[1].targetId shouldBeEqual voteOptionIds[1]
                optionCounts[2].count shouldBeEqual 0
                optionCounts[2].countType shouldBeEqual CountType.VOTE
                optionCounts[2].targetId shouldBeEqual voteOptionIds[2]
                optionCounts[3].count shouldBeEqual 0
                optionCounts[3].countType shouldBeEqual CountType.VOTE
                optionCounts[3].targetId shouldBeEqual voteOptionIds[3]
            }
        }
    }

    describe("[투표 하기] vote") {
        var posts = emptyList<Post>()
        var options = emptyList<VoteOption>()
        beforeContainer {
            val optionModels = listOf(
                VoteOptionWithoutIdModel(seq = 1, content = "11"),
                VoteOptionWithoutIdModel(seq = 2, content = "22"),
                VoteOptionWithoutIdModel(seq = 3, content = "33")
            )

            val req = CreateVoteRequest(
                content = "11",
                options = optionModels,
                boardId = boards[1].id
            )

            voteFacade.createVote(authUser, req)
            voteFacade.createVote(authUser, req)

            posts = postRepository.findAll()
            options = voteOptionRepository.findAll()
        }

        afterContainer {
            postRepository.deleteAll()
            voteOptionRepository.deleteAll()
            countRepository.deleteAll()
            voteHistoryRepository.deleteAll()
        }

        context("투표할 때 잘못된 req가 주어졌을 경우") {
            it("존재하지않은 투표면 에러") {
                val notExistVoteId = posts.last().id + 1
                val req = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = options.last().id
                )

                shouldThrow<SusuException> { voteFacade.vote(authUser, notExistVoteId, req) }
            }

            it("존재하지않은 투표 옵션이면 에러") {
                val notExistOptionId = options.last().id + 1
                val req = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = notExistOptionId
                )

                shouldThrow<NotFoundException> { voteFacade.vote(authUser, posts.last().id, req) }
            }

            it("투표에 해당하지않는 옵션이면 에러") {
                val req = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = options.first().id
                )

                shouldThrow<InvalidRequestException> { voteFacade.vote(authUser, posts.last().id, req) }
            }

            it("투표 이미 했으면 에러") {
                val voteId = posts.last().id
                val req = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = options.last().id
                )

                voteFacade.vote(authUser, voteId, req)
                shouldThrow<InvalidRequestException> { voteFacade.vote(authUser, voteId, req) }
            }

            it("동시에 여러번 투표하면 한번만 성공") {
                voteHistoryRepository.deleteAll()
                val voteId = posts.last().id
                val req = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = options.last().id
                )
                val successCount = AtomicLong()

                executeConcurrency(successCount) {
                    voteFacade.vote(authUser, voteId, req)
                }

                successCount.get() shouldBeEqual 1
            }
        }

        context("투표할 때 정상적인 req가 주어졌을 경우") {
            it("DB에 정상적으로 값이 반영되어야 한다.") {
                val voteId = posts.last().id
                val optionId = options.last().id
                val req = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = optionId
                )

                val beforePostCount = countRepository.findByTargetIdAndTargetType(voteId, CountTargetType.POST)
                val beforeOptionCount = countRepository.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION)

                voteFacade.vote(authUser, voteId, req)

                /** DB input 검증 */
                val afterPostCount = countRepository.findByTargetIdAndTargetType(voteId, CountTargetType.POST)
                val afterOptionCount = countRepository.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION)

                beforePostCount!!.count + 1 shouldBeEqual afterPostCount!!.count
                beforeOptionCount!!.count + 1 shouldBeEqual afterOptionCount!!.count
                voteHistoryRepository.findByUidAndPostId(authUser.uid, voteId) shouldNotBe null
            }
        }

        context("투표 취소할 때 잘못된 req가 주어졌을 경우") {
            it("존재하지않은 투표면 에러") {
                val notExistVoteId = posts.last().id + 1
                val req = CreateVoteHistoryRequest(
                    isCancel = true,
                    optionId = options.last().id
                )

                shouldThrow<SusuException> { voteFacade.vote(authUser, notExistVoteId, req) }
            }

            it("존재하지않은 투표 옵션이면 에러") {
                val notExistOptionId = options.last().id + 1
                val req = CreateVoteHistoryRequest(
                    isCancel = true,
                    optionId = notExistOptionId
                )

                shouldThrow<RuntimeException> { voteFacade.vote(authUser, posts.last().id, req) }
            }

            it("투표에 해당하지않는 옵션이면 에러") {
                val req = CreateVoteHistoryRequest(
                    isCancel = true,
                    optionId = options.first().id
                )

                shouldThrow<InvalidRequestException> { voteFacade.vote(authUser, posts.last().id, req) }
            }

            it("투표 안했으면 에러") {
                val voteId = posts.last().id
                val req = CreateVoteHistoryRequest(
                    isCancel = true,
                    optionId = options.last().id
                )

                shouldThrow<InvalidRequestException> { voteFacade.vote(authUser, voteId, req) }
            }
        }

        context("투표 취소할 때 정상적인 req가 주어졌을 경우") {
            beforeEach {
                val voteId = posts.last().id
                val optionId = options.last().id
                val req = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = optionId
                )

                voteFacade.vote(authUser, voteId, req)
            }

            it("DB에 정상적으로 값이 반영되어야 한다.") {
                val voteId = posts.last().id
                val optionId = options.last().id
                val req = CreateVoteHistoryRequest(
                    isCancel = true,
                    optionId = optionId
                )

                val beforePostCount = countRepository.findByTargetIdAndTargetType(voteId, CountTargetType.POST)
                val beforeOptionCount = countRepository.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION)

                voteFacade.vote(authUser, voteId, req)

                /** DB input 검증 */
                val afterPostCount = countRepository.findByTargetIdAndTargetType(voteId, CountTargetType.POST)
                val afterOptionCount = countRepository.findByTargetIdAndTargetType(optionId, CountTargetType.VOTE_OPTION)

                beforePostCount!!.count - 1 shouldBeEqual afterPostCount!!.count
                beforeOptionCount!!.count - 1 shouldBeEqual afterOptionCount!!.count
                voteHistoryRepository.findByUidAndPostId(authUser.uid, voteId) shouldBe null
            }
        }
    }

    describe("[투표 삭제하기] deleteVote") {
        var posts = emptyList<Post>()
        var options = emptyList<VoteOption>()

        beforeContainer {
            val optionModels = listOf(
                VoteOptionWithoutIdModel(seq = 1, content = "11"),
                VoteOptionWithoutIdModel(seq = 2, content = "22"),
                VoteOptionWithoutIdModel(seq = 3, content = "33")
            )

            val req = CreateVoteRequest(
                content = "11",
                options = optionModels,
                boardId = boards[1].id
            )

            voteFacade.createVote(authUser, req)

            posts = postRepository.findAll()
            options = voteOptionRepository.findAll()
        }

        afterContainer {
            postRepository.deleteAll()
            voteOptionRepository.deleteAll()
            countRepository.deleteAll()
            voteHistoryRepository.deleteAll()
        }

        context("잘못된 req가 주어졌을 때,") {
            it("존재하지 않는 투표라면 에러") {
                val notExistVoteId = posts.last().id + 1

                shouldThrow<NotFoundException> { voteFacade.deleteVote(authUser, notExistVoteId) }
            }

            it("본인이 작성한 투표가 아니라면 에러") {
                val voteId = posts.last().id
                val invalidAuthUser = AuthUserImpl(
                    uid = 2L,
                    context = AuthContextImpl(
                        name = "user2",
                        role = AccountRole.USER,
                        profileImageUrl = null,
                        userStatusTypeInfo = UserStatusTypeInfo.ACTIVE
                    )
                )

                shouldThrow<InvalidRequestException> { voteFacade.deleteVote(invalidAuthUser, voteId) }
            }
        }

        context("정상적인 req가 주어졌을 때,") {
            it("DB에 정상적으로 값이 반영되어야 한다.") {
                val voteId = posts.last().id

                voteFacade.deleteVote(authUser, voteId)

                val post = postRepository.findByIdOrNull(voteId)
                post shouldNotBe null
                post!!.isActive shouldBeEqual false

                /**
                 * TODO: event publishing 부분 test 방법 알아내면 개선 바람
                 */
                delay(1000)

                countRepository.findByTargetIdAndTargetType(voteId, CountTargetType.POST) shouldBe null
                options.forEach { option ->
                    countRepository.findByTargetIdAndTargetType(option.id, CountTargetType.VOTE_OPTION) shouldBe null
                }
            }
        }
    }

    describe("[투표 업데이트] update") {
        var post: Post? = null
        var options = emptyList<VoteOption>()

        beforeContainer {
            val optionModels = listOf(
                VoteOptionWithoutIdModel(seq = 1, content = "11"),
                VoteOptionWithoutIdModel(seq = 2, content = "22"),
                VoteOptionWithoutIdModel(seq = 3, content = "33")
            )

            val req = CreateVoteRequest(
                content = "11",
                options = optionModels,
                boardId = boards[1].id
            )

            voteFacade.createVote(authUser, req)

            post = postRepository.findAll().first()
            options = voteOptionRepository.findAll()
        }

        afterContainer {
            postRepository.deleteAll()
            voteOptionRepository.deleteAll()
            countRepository.deleteAll()
            voteHistoryRepository.deleteAll()
        }

        context("잘못된 req가 주어졌을 때") {
            it("content 길이가 ${postConfig.createForm.minContentLength} 미만이면 에러") {
                var content = ""
                for (i: Int in 1..<postConfig.createForm.minContentLength) {
                    content += "1"
                }

                val req = UpdateVoteRequest(
                    boardId = boards[0].id,
                    content = content
                )

                shouldThrow<InvalidRequestException> { voteFacade.update(authUser, post!!.id, req) }
            }

            it("content 길이가 ${postConfig.createForm.minContentLength} 이면 통과") {
                var content = ""
                for (i: Int in 1..postConfig.createForm.minContentLength) {
                    content += "1"
                }

                val req = UpdateVoteRequest(
                    boardId = boards[0].id,
                    content = content
                )

                voteFacade.update(authUser, post!!.id, req)
            }

            it("content 길이가 ${postConfig.createForm.maxContentLength} 이면 통과") {
                var content = ""
                for (i: Int in 1..<postConfig.createForm.maxContentLength) {
                    content += "1"
                }

                val req = UpdateVoteRequest(
                    boardId = boards[0].id,
                    content = content
                )

                voteFacade.update(authUser, post!!.id, req)
            }

            it("content 길이가 ${postConfig.createForm.maxContentLength} 초과면 에러") {
                var content = ""
                for (i: Int in 1..postConfig.createForm.maxContentLength) {
                    content += "1"
                }
                content += "2"

                val req = UpdateVoteRequest(
                    boardId = boards[0].id,
                    content = content
                )

                shouldThrow<InvalidRequestException> { voteFacade.update(authUser, post!!.id, req) }
            }

            it("존재하지않는 boardId면 에러") {
                val req = UpdateVoteRequest(
                    boardId = Long.MIN_VALUE,
                    content = "content"
                )

                shouldThrow<NotFoundException> { voteFacade.update(authUser, post!!.id, req) }
            }
        }

        context("정상적인 request일 때") {
            it("DB에 정상적으로 값이 반영되어야 한다.") {
                delay(2000)

                val req = UpdateVoteRequest(
                    boardId = boards[0].id,
                    content = "content"
                )

                val res = voteFacade.update(authUser, post!!.id, req)

                /** response 검증 */
                res.content shouldBeEqual req.content
                res.uid shouldBeEqual authUser.uid
                res.board.id shouldBeEqual req.boardId
                res.isModified shouldBeEqual true
                res.isMine shouldBeEqual true
                res.options.size shouldBeEqual options.size
                res.options[0].seq shouldBeEqual options[0].seq
                res.options[0].content shouldBeEqual options[0].content
                res.options[0].postId shouldBeEqual res.id
                res.options[0].isVoted shouldBeEqual false
                res.options[1].seq shouldBeEqual options[1].seq
                res.options[1].content shouldBeEqual options[1].content
                res.options[1].postId shouldBeEqual res.id
                res.options[1].isVoted shouldBeEqual false
                res.options[2].seq shouldBeEqual options[2].seq
                res.options[2].content shouldBeEqual options[2].content
                res.options[2].postId shouldBeEqual res.id
                res.options[2].isVoted shouldBeEqual false

                /** DB input 검증 */
                val posts = postRepository.findAll()
                posts.size shouldBeEqual 1
                posts[0].content shouldBeEqual req.content
                posts[0].uid shouldBeEqual authUser.uid
                posts[0].boardId shouldBeEqual req.boardId
                posts[0].type shouldBeEqual PostType.VOTE
                posts[0].isActive shouldBeEqual true
            }

            it("투표한 항목일 경우, 투표 옵션을 표시해줘야한다.") {
                delay(2000)

                val req = UpdateVoteRequest(
                    boardId = boards[0].id,
                    content = "content"
                )

                val voteId = post!!.id
                val optionId = options.last().id
                val voteReq = CreateVoteHistoryRequest(
                    isCancel = false,
                    optionId = optionId
                )
                voteFacade.vote(authUser, voteId, voteReq)

                val res = voteFacade.update(authUser, post!!.id, req)

                /** response 검증 */
                res.content shouldBeEqual req.content
                res.uid shouldBeEqual authUser.uid
                res.board.id shouldBeEqual req.boardId
                res.isModified shouldBeEqual true
                res.isMine shouldBeEqual true
                res.options.size shouldBeEqual options.size
                res.options[0].seq shouldBeEqual options[0].seq
                res.options[0].content shouldBeEqual options[0].content
                res.options[0].postId shouldBeEqual res.id
                res.options[0].isVoted shouldBeEqual false
                res.options[1].seq shouldBeEqual options[1].seq
                res.options[1].content shouldBeEqual options[1].content
                res.options[1].postId shouldBeEqual res.id
                res.options[1].isVoted shouldBeEqual false
                res.options[2].seq shouldBeEqual options[2].seq
                res.options[2].content shouldBeEqual options[2].content
                res.options[2].postId shouldBeEqual res.id
                res.options[2].isVoted shouldBeEqual true

                /** DB input 검증 */
                val posts = postRepository.findAll()
                posts.size shouldBeEqual 1
                posts[0].content shouldBeEqual req.content
                posts[0].uid shouldBeEqual authUser.uid
                posts[0].boardId shouldBeEqual req.boardId
                posts[0].type shouldBeEqual PostType.VOTE
                posts[0].isActive shouldBeEqual true
            }
        }
    }
})
