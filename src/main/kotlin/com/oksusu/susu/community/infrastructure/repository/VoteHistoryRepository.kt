package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.community.domain.VoteHistory
import com.oksusu.susu.community.domain.VoteOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VoteHistoryRepository : JpaRepository<VoteHistory, Long>{
    @Transactional(readOnly = true)
    fun existsByUidAndCommunityId(uid: Long, communityId: Long): Boolean
    @Transactional(readOnly = true)
    fun existsByUidAndCommunityIdAndVoteOptionId(uid: Long, communityId: Long, voteOptionId: Long): Boolean
    @Transactional
    fun deleteByUidAndCommunityId(uid: Long, communityId: Long)
}
