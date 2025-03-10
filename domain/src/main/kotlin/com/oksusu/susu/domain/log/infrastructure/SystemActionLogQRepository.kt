package com.oksusu.susu.domain.log.infrastructure

import com.oksusu.susu.domain.log.domain.QSystemActionLog
import com.oksusu.susu.domain.log.domain.SystemActionLog
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Transactional(readOnly = true)
interface SystemActionLogQRepository {
    fun countDau(startAt: LocalDateTime, endAt: LocalDateTime): Long
}

class SystemActionLogQRepositoryImpl : SystemActionLogQRepository,
    QuerydslRepositorySupport(SystemActionLog::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qSystemActionLog = QSystemActionLog.systemActionLog

    override fun countDau(startAt: LocalDateTime, endAt: LocalDateTime): Long {
        return JPAQuery<QSystemActionLog>(entityManager)
            .select(qSystemActionLog.uid.countDistinct())
            .from(qSystemActionLog)
            .where(qSystemActionLog.createdAt.between(startAt, endAt))
            .fetchOne() ?: 0L
    }
}
