package com.oksusu.susu.api.post.presentation

import com.oksusu.susu.api.ApiIntegrationSpec
import com.oksusu.susu.api.auth.model.AuthContextImpl
import com.oksusu.susu.api.auth.model.AuthUserImpl
import com.oksusu.susu.api.bulk.BulkService
import com.oksusu.susu.api.fixture.FixtureUtil
import com.oksusu.susu.api.post.application.VoteFacade
import com.oksusu.susu.api.post.model.BoardModel
import com.oksusu.susu.api.post.model.VoteOptionWithoutIdModel
import com.oksusu.susu.api.post.model.request.CreateVoteRequest
import com.oksusu.susu.domain.count.infrastructure.CountRepository
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.post.infrastructure.repository.BoardRepository
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.post.infrastructure.repository.VoteHistoryRepository
import com.oksusu.susu.domain.post.infrastructure.repository.VoteOptionRepository
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import io.github.oshai.kotlinlogging.KotlinLogging

class VoteResourceTest(
    private val voteResource: VoteResource,
    private val voteFacade: VoteFacade,
    private val postRepository: PostRepository,
    private val voteOptionRepository: VoteOptionRepository,
    private val countRepository: CountRepository,
    private val voteHistoryRepository: VoteHistoryRepository,
    private val boardRepository: BoardRepository,
    private val bulkService: BulkService,
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
    var posts = emptyList<Post>()
    var options = emptyList<VoteOption>()

    beforeSpec {
        boards = boardRepository.findAllByIsActive(true)
            .map { board -> BoardModel.from(board) }
            .associateBy { board -> board.id }.values
            .toList()
            .sortedBy { board -> board.id }

        bulkService.voteBulkInsert()

        posts = postRepository.findAll()
        options = voteOptionRepository.findAll()
    }

    afterSpec {
        postRepository.deleteAll()
        voteOptionRepository.deleteAll()
        countRepository.deleteAll()
        voteHistoryRepository.deleteAll()
    }

    describe("[투표 조회] getAllVotes") {
        context("차단한 글과 유저의 게시물은") {
            beforeEach {  }
            afterEach {  }
            it("조회되면 안된다.") {}
        }

        context("조회시") {
            it("글이 크기보다 적게 있다면, 다 조회된다.") {}
            it("글이 크기보다 많이 있으면, 크기만큼 조회된다."){}
            it("마지막 페이지 일 경우, 마지막이라는 flag에 표시가 되어야한다."){}
        }

        context("조건을 변경할 떄"){
            it("내 글만 보기라면, 남의 글은 보이면 안된다."){}
            it("인기순 정렬시, 투표수의 내림차순으로 정렬된다"){}
            it("boardId 지정시, 해당 board에 해당하는 글만 보인다"){}
        }
    }

    describe("[투표 하나 조회] getVote") {
        context("조회시"){
            it("올바른 값을 반환해야한다."){}
            it("투표 여부가 표시되어야 한다."){}
        }
    }

    describe("[가장 인기 있는 투표 검색] getPopularVotes") {
        context("조회시"){
            it("지정한 사이즈 만큼의 투표를 검색한다."){}
            it("반환한 값은 인기순으로 상위에 위치는 값이어야 한다."){}
        }
    }
})
