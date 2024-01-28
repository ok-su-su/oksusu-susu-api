package com.oksusu.susu.post.model.vo

/** vote 조회 조건 */
data class SearchVoteRequest(
    /** 제목 */
    val content: String?,
    /** 본인 소유 글 여부 / 내 글 : 1, 전체 글 : 0 or null */
    val mine: Boolean?,
    /** 정렬 기준 / 최신순 : LATEST, 투표 많은 순 : POPULAR */
    val sortType: VoteSortType?,
    /** 보드 id */
    val boardId: Long?,
)

enum class VoteSortType {
    /** 최신 작성 순 */
    LATEST,

    /** 투표 많은 순 */
    POPULAR,
    ;
}
