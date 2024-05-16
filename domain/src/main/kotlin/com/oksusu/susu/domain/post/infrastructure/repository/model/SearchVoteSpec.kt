package com.oksusu.susu.domain.post.infrastructure.repository.model

/** 투표 검색 조건 */
data class SearchVoteSpec(
    /** 검색 내용 */
    val content: String?,
    /** 본인 소유 여부 */
    val mine: Boolean?,
    /** 정렬 기준 */
    val sortType: VoteSortType,
    /** board id */
    val boardId: Long?,
)

enum class VoteSortType {
    /** 최신 작성 순 */
    LATEST,

    /** 투표 많은 순 */
    POPULAR,
    ;
}
