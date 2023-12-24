package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.community.domain.VoteHistory
import com.oksusu.susu.community.domain.VoteOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VoteHistoryRepository : JpaRepository<VoteHistory, Long>{
    fun existsByUidAndCommunityId(uid: Long, communityId: Long): Boolean
    fun existsByUidAndCommunityIdAndVoteOptionId(uid: Long, communityId: Long, voteOptionId: Long): Boolean
    fun deleteByUidAndCommunityId(uid: Long, communityId: Long)
}