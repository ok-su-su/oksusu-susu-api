package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.QUserStatusHistory
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.querydsl.jpa.impl.JPAQuery
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
interface UserStatusHistoryQRepository {
    fun getUidByToStatusIdAfter(toStatusId: Long, targetDate: LocalDateTime): List<Long>

    fun getUidByToStatusId(toStatusId: Long): List<Long>

    fun updateAllCreatedAt(createdAt: LocalDateTime): Long
}

class UserStatusHistoryQRepositoryImpl : UserStatusHistoryQRepository,
    QuerydslRepositorySupport(UserStatusHistory::class.java) {

    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qUserStatusHistory = QUserStatusHistory.userStatusHistory

    override fun getUidByToStatusIdAfter(toStatusId: Long, targetDate: LocalDateTime): List<Long> {
        return JPAQuery<QUserStatusHistory>(entityManager)
            .select(qUserStatusHistory.uid)
            .from(qUserStatusHistory)
            .where(
                qUserStatusHistory.toStatusId.eq(toStatusId),
                qUserStatusHistory.createdAt.after(targetDate)
            )
            .fetch()
    }

    override fun getUidByToStatusId(toStatusId: Long): List<Long> {
        return JPAQuery<QUserStatusHistory>(entityManager)
            .select(qUserStatusHistory.uid)
            .from(qUserStatusHistory)
            .where(
                qUserStatusHistory.toStatusId.eq(toStatusId)
            )
            .fetch()
    }

    override fun updateAllCreatedAt(createdAt: LocalDateTime): Long {
        return JPAQueryFactory(entityManager)
            .update(qUserStatusHistory)
            .set(qUserStatusHistory.createdAt, createdAt)
            .execute()
    }
}
