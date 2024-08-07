package com.oksusu.susu.domain.post.infrastructure.repository

import com.oksusu.susu.domain.common.extension.isEquals
import com.oksusu.susu.domain.count.domain.QCount
import com.oksusu.susu.domain.count.domain.vo.CountTargetType
import com.oksusu.susu.domain.post.domain.QPost
import com.oksusu.susu.domain.post.domain.QVoteOption
import com.oksusu.susu.domain.post.domain.VoteOption
import com.oksusu.susu.domain.post.infrastructure.repository.model.QVoteOptionAndCountModel
import com.oksusu.susu.domain.post.infrastructure.repository.model.VoteOptionAndCountModel
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Transactional

@Transactional(readOnly = true)
interface VoteOptionQRepository {
    fun getOptionAndCount(postId: Long): List<VoteOptionAndCountModel>
}

class VoteOptionQRepositoryImpl : VoteOptionQRepository, QuerydslRepositorySupport(VoteOption::class.java) {
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
            .where(qVoteOption.postId.isEquals(postId))
            .fetch()
    }
}
