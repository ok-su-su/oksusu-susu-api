package com.oksusu.susu.domain.post.infrastructure.repository

import com.oksusu.susu.domain.post.domain.VoteOption
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VoteOptionRepository : JpaRepository<VoteOption, Long>, VoteOptionQRepository {
    @Transactional(readOnly = true)
    fun findAllByPostIdInOrderBySeq(postIds: List<Long>): List<VoteOption>

    @Transactional(readOnly = true)
    fun findAllByPostIdOrderBySeq(postId: Long): List<VoteOption>
}
