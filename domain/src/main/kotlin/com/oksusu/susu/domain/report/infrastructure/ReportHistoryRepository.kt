package com.oksusu.susu.domain.report.infrastructure

import com.oksusu.susu.domain.report.domain.QReportHistory
import com.oksusu.susu.domain.report.domain.ReportHistory
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface ReportHistoryRepository : JpaRepository<ReportHistory, Long>, ReportHistoryCustomRepository {
    @Transactional(readOnly = true)
    fun existsByUidAndTargetIdAndTargetType(uid: Long, targetId: Long, targetType: ReportTargetType): Boolean

    @Transactional(readOnly = true)
    fun findAllByCreatedAtAfter(createdAt: LocalDateTime): List<ReportHistory>

    @Transactional(readOnly = true)
    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long
}

interface ReportHistoryCustomRepository {
    @Transactional
    fun updateAllCreatedAt(createdAt: LocalDateTime): Long
}

class ReportHistoryCustomRepositoryImpl : ReportHistoryCustomRepository,
    QuerydslRepositorySupport(ReportHistory::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qReportHistory = QReportHistory.reportHistory

    override fun updateAllCreatedAt(createdAt: LocalDateTime): Long {
        return JPAQueryFactory(entityManager)
            .update(qReportHistory)
            .set(qReportHistory.createdAt, createdAt)
            .execute()
    }
}
