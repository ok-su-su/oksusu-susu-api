package com.oksusu.susu.api.post.presentation

import com.oksusu.susu.api.ApiIntegrationSpec
import com.oksusu.susu.api.auth.model.AdminUserImpl
import com.oksusu.susu.api.auth.model.AuthContextImpl
import com.oksusu.susu.api.auth.model.AuthUserImpl
import com.oksusu.susu.api.post.application.VoteFacade
import com.oksusu.susu.api.post.model.BoardModel
import com.oksusu.susu.api.post.model.VoteOptionWithoutIdModel
import com.oksusu.susu.api.post.model.request.CreateVoteRequest
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
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
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.delay
import org.junit.jupiter.api.Assertions.*
import org.springframework.data.repository.findByIdOrNull

class VoteAdminResourceTest(
    private val voteAdminResource: VoteAdminResource,
    private val boardRepository: BoardRepository,
    private val voteFacade: VoteFacade,
    private val postRepository: PostRepository,
    private val voteOptionRepository: VoteOptionRepository,
    private val countRepository: CountRepository,
    private val voteHistoryRepository: VoteHistoryRepository,
) : ApiIntegrationSpec({

    val adminUser = AdminUserImpl(
        uid = 1L
    )
    val authUser = AuthUserImpl(
        uid = 1L,
        context = AuthContextImpl(
            name = "admin",
            role = AccountRole.ADMIN,
            profileImageUrl = null,
            userStatusTypeInfo = UserStatusTypeInfo.ACTIVE
        )
    )

    describe("[게시글 카테고리 데이터 제공] getCreatePostsConfig") {
        var boards = emptyList<BoardModel>()
        var posts = emptyList<Post>()
        var options = emptyList<VoteOption>()

        beforeContainer {
            boards = boardRepository.findAllByIsActive(true)
                .map { board -> BoardModel.from(board) }
                .associateBy { board -> board.id }.values
                .toList()
                .sortedBy { board -> board.id }

            val optionModels = listOf(
                VoteOptionWithoutIdModel(seq = 1, content = "11"),
                VoteOptionWithoutIdModel(seq = 2, content = "22"),
                VoteOptionWithoutIdModel(seq = 3, content = "33")
            )

            val req = CreateVoteRequest(
                content = "11",
                options = optionModels,
                boardId = boards[0].id
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

        context("삭제시") {
            it("DB에 정상적으로 값이 반영되어야 한다.") {
                val voteId = posts.last().id

                voteAdminResource.deletePost(user = adminUser, PostType.VOTE, voteId)

                val post = postRepository.findByIdOrNull(voteId)
                post shouldNotBe null
                post!!.isActive shouldBeEqual false

                /**
                 * TODO: event publishing 부분 test 방법 알아내면 개선 바람
                 */
                delay(2000)

                countRepository.findByTargetIdAndTargetType(voteId, CountTargetType.POST) shouldBe null
                options.forEach { option ->
                    countRepository.findByTargetIdAndTargetType(option.id, CountTargetType.VOTE_OPTION) shouldBe null
                }
            }
        }
    }
})
