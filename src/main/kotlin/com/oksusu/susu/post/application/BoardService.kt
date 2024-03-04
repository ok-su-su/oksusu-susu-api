package com.oksusu.susu.post.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.NotFoundException
import com.oksusu.susu.extension.resolveCancellation
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.post.domain.Board
import com.oksusu.susu.post.infrastructure.repository.BoardRepository
import com.oksusu.susu.post.model.BoardModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
class BoardService(
    private val boardRepository: BoardRepository,
) {
    private val logger = KotlinLogging.logger { }
    private var boards: Map<Long, BoardModel> = emptyMap()

    @Scheduled(
        fixedRate = 1000 * 60 * 3,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-boards.initial-delay:0}"
    )
    fun refreshBoards() {
        CoroutineScope(Dispatchers.IO).launch {
            logger.info { "start refresh boards" }

            boards = runCatching {
                findAllByIsActive(true)
                    .map { board -> BoardModel.from(board) }
                    .associateBy { board -> board.id }
            }.onFailure { e ->
                logger.resolveCancellation("refreshBoards", e)
            }.getOrDefault(boards)

            logger.info { "finish refresh boards" }
        }
    }

    suspend fun getAll(): List<BoardModel> {
        return boards.values.toList()
    }

    suspend fun findAllByIsActive(isActive: Boolean): List<Board> {
        return withMDCContext(Dispatchers.IO) { boardRepository.findAllByIsActive(isActive) }
    }

    fun getBoard(id: Long): BoardModel {
        return boards[id] ?: throw NotFoundException(ErrorCode.NOT_FOUND_BOARD_ERROR)
    }

    fun validateExistBoard(boardId: Long) {
        getBoard(boardId)
    }
}
