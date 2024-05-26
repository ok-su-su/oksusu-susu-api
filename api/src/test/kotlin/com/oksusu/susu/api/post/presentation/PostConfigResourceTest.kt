package com.oksusu.susu.api.post.presentation

import com.oksusu.susu.api.ApiIntegrationSpec
import com.oksusu.susu.api.auth.model.AuthContextImpl
import com.oksusu.susu.api.auth.model.AuthUserImpl
import com.oksusu.susu.api.post.application.BoardService
import com.oksusu.susu.api.post.model.BoardModel
import com.oksusu.susu.api.testExtension.getBodyOrThrow
import com.oksusu.susu.domain.post.infrastructure.repository.BoardRepository
import com.oksusu.susu.domain.user.domain.vo.AccountRole
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.equals.shouldBeEqual
import org.junit.jupiter.api.Assertions.*

class PostConfigResourceTest(
    private val postConfigResource: PostConfigResource,
    private val boardService: BoardService,
    private val boardRepository: BoardRepository,
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

    describe("[게시글 카테고리 데이터 제공] getCreatePostsConfig") {
        context("조회시") {
            it("올바른 config 값이 조회되어야 한다.") {
                val res = postConfigResource.getCreatePostsConfig(authUser)

                val models = boardRepository.findAllByIsActive(true)
                    .map { board -> BoardModel.from(board) }
                    .associateBy { board -> board.id }
                    .values
                    .toList()

                models shouldBeEqual res.getBodyOrThrow()
            }
        }
    }
})
