package com.oksusu.susu.post.infrastructure.repository

import com.oksusu.susu.post.domain.VoteOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VoteOptionRepository : JpaRepository<VoteOption, Long> {
    @Transactional(readOnly = true)
    fun findAllByPostIdInOrderBySeq(postIds: List<Long>): List<VoteOption>

    @Transactional(readOnly = true)
    fun findAllByPostIdOrderBySeq(postId: Long): List<VoteOption>
}
