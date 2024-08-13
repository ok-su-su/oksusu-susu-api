package com.oksusu.susu.domain.report.infrastructure

import com.oksusu.susu.domain.report.domain.QReportHistory
import com.oksusu.susu.domain.report.domain.ReportHistory
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
interface ReportHistoryQRepository {
    @Transactional
    fun updateAllCreatedAt(createdAt: LocalDateTime): Long
}

class ReportHistoryQRepositoryImpl : ReportHistoryQRepository,
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
