package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.ApiIntegrationSpec
import com.oksusu.susu.api.post.model.VoteOptionWithoutIdModel
import com.oksusu.susu.api.post.model.request.CreateVoteRequest
import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.domain.post.domain.VoteOption
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow

class VoteValidateServiceTest(
    private val voteValidateService: VoteValidateService,
    private val postConfig: SusuConfig.PostConfig,
) : ApiIntegrationSpec({
    val logger = KotlinLogging.logger { }

    describe("투표 생성 request validation") {
        val validOptions = listOf(
            VoteOption(seq = 1, content = "1", postId = 1L),
            VoteOption(seq = 2, content = "2", postId = 2L)
        )

        context("content 길이가") {
            it("0이면 에러") {
                val req = CreateVoteRequest(
                    content = "",
                    options = validOptions
                        .map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )
                shouldThrow<InvalidRequestException> { voteValidateService.validateCreateVoteRequest(req) }
            }

            it("1이면 통과") {
                val req = CreateVoteRequest(
                    content = "1",
                    options = validOptions
                        .map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )
                voteValidateService.validateCreateVoteRequest(req)
            }

            it("${postConfig.createForm.maxContentLength}이면 통과") {
                var content = ""
                for (i: Int in 1..postConfig.createForm.maxContentLength) {
                    content += "1"
                }

                val req = CreateVoteRequest(
                    content = content,
                    options = validOptions
                        .map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )
                voteValidateService.validateCreateVoteRequest(req)
            }

            it("${postConfig.createForm.maxContentLength} 초과면 에러") {
                var content = ""
                for (i: Int in 1..postConfig.createForm.maxContentLength) {
                    content += "1"
                }
                content += "2"

                val req = CreateVoteRequest(
                    content = content,
                    options = validOptions
                        .map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )
                shouldThrow<InvalidRequestException> { voteValidateService.validateCreateVoteRequest(req) }
            }
        }

        context("option 개수가") {
            it("0이면 에러") {
                val req = CreateVoteRequest(
                    content = "0",
                    options = emptyList(),
                    boardId = 1
                )
                shouldThrow<InvalidRequestException> { voteValidateService.validateCreateVoteRequest(req) }
            }

            it("1 이상이면 통과") {
                val req = CreateVoteRequest(
                    content = "0",
                    options = validOptions
                        .map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )
                voteValidateService.validateCreateVoteRequest(req)
            }
        }

        context("option content 길이가") {
            it("0이면 에러") {
                val options = listOf(
                    VoteOption(seq = 1, content = "", postId = 1L),
                    VoteOption(seq = 2, content = "", postId = 2L)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options.map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )

                shouldThrow<InvalidRequestException> { voteValidateService.validateCreateVoteRequest(req) }
            }

            it("1이면 통과") {
                val options = listOf(
                    VoteOption(seq = 1, content = "1", postId = 1L),
                    VoteOption(seq = 2, content = "2", postId = 2L)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options.map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )

                voteValidateService.validateCreateVoteRequest(req)
            }

            it("${postConfig.createVoteOptionForm.maxContentLength}이면 통과") {
                var content = ""
                for (i: Int in 1..postConfig.createVoteOptionForm.maxContentLength) {
                    content += "1"
                }

                val options = listOf(
                    VoteOption(seq = 1, content = content, postId = 1L),
                    VoteOption(seq = 2, content = content, postId = 2L)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options.map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )

                voteValidateService.validateCreateVoteRequest(req)
            }

            it("${postConfig.createVoteOptionForm.maxContentLength} 초과면 에러") {
                var content = ""
                for (i: Int in 1..postConfig.createVoteOptionForm.maxContentLength) {
                    content += "1"
                }
                content += "2"

                val options = listOf(
                    VoteOption(seq = 1, content = content, postId = 1L),
                    VoteOption(seq = 2, content = content, postId = 2L)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options.map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )

                shouldThrow<InvalidRequestException> { voteValidateService.validateCreateVoteRequest(req) }
            }
        }

        context("seq가"){
            it("중복되면 에러"){
                val options = listOf(
                    VoteOption(seq = 1, content = "1", postId = 1L),
                    VoteOption(seq = 1, content = "2", postId = 2L)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options.map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )

                shouldThrow<InvalidRequestException> { voteValidateService.validateCreateVoteRequest(req) }

            }

            it("올바르면 통과"){
                val options = listOf(
                    VoteOption(seq = 1, content = "1", postId = 1L),
                    VoteOption(seq = 2, content = "2", postId = 2L)
                )
                val req = CreateVoteRequest(
                    content = "0",
                    options = options.map { option -> VoteOptionWithoutIdModel(option.content, option.seq) },
                    boardId = 1
                )

                voteValidateService.validateCreateVoteRequest(req)
            }
        }
    }
})
