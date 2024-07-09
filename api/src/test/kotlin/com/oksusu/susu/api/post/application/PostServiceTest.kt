package com.oksusu.susu.api.post.application

import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.domain.post.domain.vo.PostType
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.post.infrastructure.repository.model.PostAndUserModel
import fixture.DomainFixtureUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import org.springframework.data.repository.findByIdOrNull

class PostServiceTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val mockPostRepository = mockk<PostRepository>()

    val postService = PostService(mockPostRepository)

    describe("조회") {
        context("id로 post 조회시") {
            every { mockPostRepository.findByIdOrNull(Long.MIN_VALUE) } returns null
            every { mockPostRepository.findByIdOrNull(1L) } returns DomainFixtureUtil.getPostBuilder().set("id", 1).sample()

            it("없으면 에러 발생") {
                shouldThrow<NotFoundException> { postService.findByIdOrThrow(Long.MIN_VALUE) }
            }

            it("있으면 통과") {
                val post = postService.findByIdOrThrow(1L)

                post.id shouldBeEqual 1L
            }
        }

        context("id, isActive, type로 post 조회시") {
            every { mockPostRepository.findByIdAndIsActiveAndType(Long.MIN_VALUE, any(), any()) } returns null
            every {
                mockPostRepository.findByIdAndIsActiveAndType(
                    1L,
                    true,
                    PostType.VOTE
                )
            } returns DomainFixtureUtil.getPostBuilder().set("id", 1).set("isActive", true).set("type", PostType.VOTE)
                .sample()

            it("없으면 에러 발생") {
                shouldThrow<NotFoundException> {
                    postService.findByIdAndIsActiveAndTypeOrThrow(Long.MIN_VALUE, true, PostType.VOTE)
                }
            }

            it("있으면 통과") {
                val post = postService.findByIdAndIsActiveAndTypeOrThrow(1L, true, PostType.VOTE)

                post.id shouldBeEqual 1L
                post.isActive shouldBeEqual true
                post.type shouldBeEqual PostType.VOTE
            }
        }

        context("id, type으로 post, user 조회시") {
            every { mockPostRepository.getPostAndCreator(Long.MIN_VALUE, any()) } returns null
            every { mockPostRepository.getPostAndCreator(1L, PostType.VOTE) } returns PostAndUserModel(
                DomainFixtureUtil.getPostBuilder().set("id", 1).set("type", PostType.VOTE).sample(),
                DomainFixtureUtil.getUser()
            )

            it("없으면 에러") {
                shouldThrow<NotFoundException> { postService.getPostAndCreator(Long.MIN_VALUE, PostType.VOTE) }
            }

            it("있으면 그대로 진행") {
                val model = postService.getPostAndCreator(1L, PostType.VOTE)

                model.post.id shouldBeEqual 1L
                model.post.type shouldBeEqual PostType.VOTE
            }
        }
    }

    describe("validation") {
        context("id로 post 존재 여부 검증시") {
            every { mockPostRepository.existsById(Long.MIN_VALUE) } returns false
            every { mockPostRepository.existsById(1L) } returns true

            it("없으면 에러") {
                shouldThrow<InvalidRequestException> { postService.validateExist(Long.MIN_VALUE) }
            }

            it("있으면 그대로 진행") {
                postService.validateExist(1L)
            }
        }

        context("post 소유권 검증시") {
            every { mockPostRepository.findByIdOrNull(1L) } returns DomainFixtureUtil.getPostBuilder().set("id", 1)
                .set("uid", 1).sample()

            it("본인꺼 아니면 에러") {
                shouldThrow<InvalidRequestException> { postService.validateAuthority(1L, Long.MAX_VALUE) }
            }
            it("본인꺼면 진행") {
                postService.validateAuthority(1L, 1L)
            }
        }
    }
})
