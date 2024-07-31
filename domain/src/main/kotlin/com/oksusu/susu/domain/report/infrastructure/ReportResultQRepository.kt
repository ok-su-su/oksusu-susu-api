package com.oksusu.susu.domain.report.infrastructure

import com.oksusu.susu.domain.report.domain.QReportResult
import com.oksusu.susu.domain.report.domain.ReportResult
import com.querydsl.jpa.impl.JPAQueryFactory
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
interface ReportResultQRepository {
    fun updateAllCreatedAt(createdAt: LocalDateTime): Long
}

class ReportResultQRepositoryImpl : ReportResultQRepository,
    QuerydslRepositorySupport(ReportResult::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qReportResult = QReportResult.reportResult

    override fun updateAllCreatedAt(createdAt: LocalDateTime): Long {
        return JPAQueryFactory(entityManager)
            .update(qReportResult)
            .set(qReportResult.createdAt, createdAt)
            .execute()
    }
}
