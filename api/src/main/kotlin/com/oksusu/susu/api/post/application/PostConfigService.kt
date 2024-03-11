package com.oksusu.susu.api.post.application

import com.oksusu.susu.api.post.model.BoardModel
import org.springframework.stereotype.Service

@Service
class PostConfigService(
    private val boardService: BoardService,
) {
    suspend fun getAll(): List<BoardModel> {
        return boardService.getAll()
    }
}
