package com.oksusu.susu.domain.user.infrastructure

import com.oksusu.susu.domain.user.domain.QUserStatusHistory
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface UserStatusHistoryRepository : JpaRepository<UserStatusHistory, Long>, UserStatusHistoryCustomRepository {
    @Transactional(readOnly = true)
    fun findAllByIsForcedAndStatusAssignmentTypeAndToStatusId(
        isForced: Boolean,
        assignmentType: UserStatusAssignmentType,
        toStatusId: Long,
    ): List<UserStatusHistory>
}

interface UserStatusHistoryCustomRepository {
    @Transactional(readOnly = true)
    fun getUidByToStatusIdAfter(toStatusId: Long, targetDate: LocalDateTime): List<Long>

    @Transactional(readOnly = true)
    fun getUidByToStatusId(toStatusId: Long): List<Long>
}

class UserStatusHistoryCustomRepositoryImpl : UserStatusHistoryCustomRepository,
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
}
