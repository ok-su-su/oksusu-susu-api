package com.oksusu.susu.api.post.infrastructure.repository.model

import com.oksusu.susu.api.post.model.vo.SearchVoteRequest
import com.oksusu.susu.api.post.model.vo.VoteSortType

/** 투표 검색 조건 */
class SearchVoteSpec(
    /** 검색 내용 */
    val content: String?,
    /** 본인 소유 여부 */
    val mine: Boolean?,
    /** 정렬 기준 */
    val sortType: VoteSortType,
    /** board id */
    val boardId: Long?,
) {
    companion object {
        fun from(request: SearchVoteRequest): SearchVoteSpec {
            return SearchVoteSpec(
                content = request.content,
                mine = request.mine,
                sortType = request.sortType ?: VoteSortType.LATEST,
                boardId = request.boardId
            )
        }

        fun defaultPopularSpec(): SearchVoteSpec {
            return SearchVoteSpec(
                content = null,
                mine = null,
                sortType = VoteSortType.POPULAR,
                boardId = null
            )
        }
    }
}
