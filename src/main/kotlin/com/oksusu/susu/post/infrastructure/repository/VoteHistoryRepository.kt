package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.post.domain.VoteHistory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VoteHistoryRepository : JpaRepository<VoteHistory, Long> {
    @Transactional(readOnly = true)
    fun existsByUidAndPostId(uid: Long, postId: Long): Boolean

    @Transactional(readOnly = true)
    fun existsByUidAndPostIdAndVoteOptionId(uid: Long, postId: Long, voteOptionId: Long): Boolean

    @Transactional
    fun deleteByUidAndPostId(uid: Long, postId: Long)

    @Transactional(readOnly = true)
    fun findByUidAndPostId(uid: Long, postId: Long): VoteHistory?

    @Transactional(readOnly = true)
    fun existsByPostId(postId: Long): Boolean
}
