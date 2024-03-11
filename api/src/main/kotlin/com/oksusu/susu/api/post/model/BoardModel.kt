package com.oksusu.susu.api.post.model

import com.oksusu.susu.api.post.domain.Board

/** 보드 모델 */
data class BoardModel(
    /** 보드 id */
    val id: Long,
    /** 보드 명 */
    val name: String,
    /** 보드 순서 */
    val seq: Int,
    /** 활성화 여부 / 활성화 : 1, 비활성화 : 0 */
    var isActive: Boolean,
) {
    companion object {
        fun from(board: Board): BoardModel {
            return BoardModel(
                id = board.id,
                name = board.name,
                seq = board.seq,
                isActive = board.isActive
            )
        }
    }
}
