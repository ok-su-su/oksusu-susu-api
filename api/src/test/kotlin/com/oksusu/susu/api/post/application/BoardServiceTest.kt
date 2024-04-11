package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.fixture.FixtureUtil
import com.oksusu.susu.common.exception.NotFoundException
import com.oksusu.susu.domain.post.infrastructure.repository.BoardRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.*
import kotlinx.coroutines.test.*

class BoardServiceTest : DescribeSpec({
    val logger = KotlinLogging.logger { }

    val mockBoardRepository = mockk<BoardRepository>()

    val boardService = BoardService(mockBoardRepository)

    every { mockBoardRepository.findAllByIsActive(any()) } returns FixtureUtil.getBoards(5)

    beforeSpec {
        boardService.refreshBoards()
    }

    describe("scheduler") {
        context("run 될 경우") {
            it("board data를 리프레시 해야한다.") {
                runTest {
                    val boards = boardService.getAll()

                    boards.size shouldBeEqual 5
                }
            }
        }
    }

    describe("validation") {
        context("board 존재여부 검증시") {
            it("존재하지않는 보드일 경우 에러 발생") {
                val boardIds = boardService.getAll().map { board -> board.id }



                shouldThrow<NotFoundException> {
                    boardService.validateExistBoard(Long.MAX_VALUE)
                }
            }

            it("존재하면 정상 진행") {

            }
        }
    }
})
