package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.community.domain.VoteOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VoteOptionRepository : JpaRepository<VoteOption, Long> {
    @Transactional(readOnly = true)
    fun findAllByCommunityIdInOrderBySeq(communityIds: List<Long>): List<VoteOption>

    @Transactional(readOnly = true)
    fun findAllByCommunityIdOrderBySeq(communityId: Long): List<VoteOption>
}
