package com.oksusu.susu.post.application

import com.oksusu.susu.post.model.BoardModel
import org.springframework.stereotype.Service

@Service
class PostConfigService(
    private val boardService: BoardService,
) {
    suspend fun getAll(): List<BoardModel> {
        return boardService.getAll()
    }
}
