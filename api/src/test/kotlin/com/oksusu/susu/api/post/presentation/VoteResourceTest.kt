package com.oksusu.susu.api.post.presentation

import com.oksusu.susu.api.ApiIntegrationSpec

class VoteResourceTest(
    private val voteResource: VoteResource,
) : ApiIntegrationSpec({
    describe("[투표 조회] getAllVotes") {
        context("차단한 글과 유저의 게시물은") {
            it("조회되면 안된다.") {}
        }

        context("조회시") {
            it("글이 크기보다 적게 있다면, 다 조회된다.") {}
            it("글이 크기보다 많이 있으면, 크기만큼 조회된다."){}
            it("마지막 페이지 일 경우, 마지막이라는 flag에 표시가 되어야한다.")
        }

        context("조건을 변경할 떄"){
            it("내 글만 보기라면, 남의 글은 보이면 안된다."){}
            it("인기순 정렬시, 투표수의 내림차순으로 정렬된다"){}
            it("boardId 지정시, 해당 board에 해당하는 글만 보인다"){}
        }
    }

    describe("[투표 하나 조회] getVote") {
        context("조회시"){
            it("올바른 값을 반환해야한다."){}
            it("투표 여부가 표시되어야 한다."){}
        }
    }

    describe("[가장 인기 있는 투표 검색] getPopularVotes") {
        context("조회시"){
            it("지정한 사이즈 만큼의 투표를 검색한다."){}
            it("반환한 값은 인기순으로 상위에 위치는 값이어야 한다.")
        }
    }
})
