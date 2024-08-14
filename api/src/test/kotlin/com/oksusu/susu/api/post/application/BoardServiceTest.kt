package com.oksusu.susu.api.post.application

import com.oksusu.susu.client.common.coroutine.ErrorPublishingCoroutineExceptionHandler
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.domain.post.infrastructure.repository.BoardRepository
import fixture.DomainFixtureUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.delay

class BoardServiceTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val mockBoardRepository = mockk<BoardRepository>()
    val mockCoroutineExceptionHandler = mockk<ErrorPublishingCoroutineExceptionHandler>()

    val boardService = BoardService(mockBoardRepository, mockCoroutineExceptionHandler)

    every { mockBoardRepository.findAllByIsActive(any()) } returns DomainFixtureUtil.getBoards(5)
    every { mockCoroutineExceptionHandler.handler } returns CoroutineExceptionHandler { _, _ -> }

    beforeSpec {
        boardService.refreshBoards()

        delay(100)
    }

    describe("scheduler") {
        context("run 될 경우") {
            it("board data를 리프레시 해야한다.") {
                val boards = boardService.getAll()
                logger.info { boards }

                boards.size shouldBeEqual 5
            }
        }
    }

    describe("validation") {
        context("board 존재여부 검증시") {
            it("존재하지않는 보드일 경우 에러 발생") {
                shouldThrow<NotFoundException> {
                    boardService.validateExistBoard(Long.MIN_VALUE)
                }
            }

            it("존재하면 정상 진행") {
                val boardIds = boardService.getAll().map { board -> board.id }

                boardService.validateExistBoard(boardId = boardIds[0])
            }
        }
    }
})
