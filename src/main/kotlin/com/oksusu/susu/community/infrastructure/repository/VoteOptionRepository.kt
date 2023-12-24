package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.community.domain.VoteOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteOptionRepository : JpaRepository<VoteOption, Long> {
    fun findAllByCommunityIdInOrderBySeq(communityIds: List<Long>): List<VoteOption>
    fun findAllByCommunityIdOrderBySeq(communityId: Long) : List<VoteOption>
}
