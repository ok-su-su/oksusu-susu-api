package com.oksusu.susu.api.post.infrastructure.repository

import com.oksusu.susu.api.count.domain.QCount
import com.oksusu.susu.api.count.domain.vo.CountTargetType
import com.oksusu.susu.api.extension.isEquals
import com.oksusu.susu.api.post.domain.QPost
import com.oksusu.susu.api.post.domain.QVoteOption
import com.oksusu.susu.api.post.domain.VoteOption
import com.oksusu.susu.api.post.infrastructure.repository.model.VoteOptionAndCountModel
import com.oksusu.susu.api.post.infrastructure.repository.model.*
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface VoteOptionRepository : JpaRepository<VoteOption, Long>, VoteOptionCustomRepository {
    @Transactional(readOnly = true)
    fun findAllByPostIdInOrderBySeq(postIds: List<Long>): List<VoteOption>

    @Transactional(readOnly = true)
    fun findAllByPostIdOrderBySeq(postId: Long): List<VoteOption>
}

interface VoteOptionCustomRepository {
    @Transactional(readOnly = true)
    fun getOptionAndCount(postId: Long): List<VoteOptionAndCountModel>
}

class VoteOptionCustomRepositoryImpl : VoteOptionCustomRepository, QuerydslRepositorySupport(VoteOption::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qVoteOption = QVoteOption.voteOption
    private val qCount = QCount.count1
    override fun getOptionAndCount(postId: Long): List<VoteOptionAndCountModel> {
        return JPAQuery<QPost>(entityManager)
            .select(QVoteOptionAndCountModel(qVoteOption, qCount.count))
            .from(qVoteOption)
            .join(qCount).on(qCount.targetType.eq(CountTargetType.VOTE_OPTION).and(qVoteOption.id.eq(qCount.targetId)))
            .where(
                qVoteOption.postId.isEquals(postId)
            ).fetch()
    }
}
