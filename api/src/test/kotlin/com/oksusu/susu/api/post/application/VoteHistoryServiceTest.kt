package com.oksusu.susu.api.post.application

import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.domain.post.infrastructure.repository.VoteHistoryRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.mockk.every
import io.mockk.mockk

class VoteHistoryServiceTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val mockVoteHistoryRepository = mockk<VoteHistoryRepository>()

    val voteHistoryService = VoteHistoryService(mockVoteHistoryRepository)

    describe("validation") {
        context("uid, postId, voteOptionId로 투표 기록 존재 여부 검증시") {
            every { mockVoteHistoryRepository.existsByUidAndPostIdAndVoteOptionId(1L, 1L, 1L) } returns true
            every { mockVoteHistoryRepository.existsByUidAndPostIdAndVoteOptionId(2L, 2L, 2L) } returns false

            it("없으면 에러") {
                shouldThrow<InvalidRequestException> { voteHistoryService.validateVoteExist(2L, 2L, 2L) }
            }

            it("투표 기록 있으면 그대로 진행") {
                voteHistoryService.validateVoteExist(1L, 1L, 1L)
            }
        }

        context("uid, postId로 투표 기록 존재 여부 검증시") {
            every { mockVoteHistoryRepository.existsByUidAndPostId(1L, 1L) } returns true
            every { mockVoteHistoryRepository.existsByUidAndPostId(2L, 2L) } returns false

            it("있으면 에러") {
                shouldThrow<InvalidRequestException> { voteHistoryService.validateVoteNotExist(1L, 1L) }
            }

            it("없으면 그대로 진행") {
                voteHistoryService.validateVoteNotExist(2L, 2L)
            }
        }
    }
})
