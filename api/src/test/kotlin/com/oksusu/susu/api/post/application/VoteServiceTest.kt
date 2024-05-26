package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.fixture.FixtureUtil
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.post.infrastructure.repository.model.PostAndVoteOptionModel
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import org.junit.jupiter.api.Assertions.*

class VoteServiceTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val mockPostService = mockk<PostService>()
    val mockPostRepository = mockk<PostRepository>()

    val voteService = VoteService(postService = mockPostService, postRepository = mockPostRepository)

    describe("조회") {
        context("vote와 post 조회시") {
            every { mockPostRepository.getVoteAndOptions(Long.MIN_VALUE) } returns emptyList()
            every { mockPostRepository.getVoteAndOptions(1L) } returns listOf(
                PostAndVoteOptionModel(FixtureUtil.getPost(), FixtureUtil.getVoteOption())
            )

            it("없으면 에러") {
                shouldThrow<NotFoundException> { voteService.getVoteAndOptions(Long.MIN_VALUE) }
            }

            it("있으면 값 반환") {
                val models = voteService.getVoteAndOptions(1L)

                models.size shouldBeEqual 1
            }
        }
    }
})
