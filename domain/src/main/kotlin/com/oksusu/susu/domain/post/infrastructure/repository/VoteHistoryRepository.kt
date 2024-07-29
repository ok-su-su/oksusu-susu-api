package com.oksusu.susu.domain.post.infrastructure.repository

import com.oksusu.susu.domain.post.domain.VoteHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
@Repository
interface VoteHistoryRepository : JpaRepository<VoteHistory, Long> {
    fun existsByUidAndPostId(uid: Long, postId: Long): Boolean

    fun existsByUidAndPostIdAndVoteOptionId(uid: Long, postId: Long, voteOptionId: Long): Boolean

    fun deleteByUidAndPostId(uid: Long, postId: Long)

    fun findByUidAndPostId(uid: Long, postId: Long): VoteHistory?
}
