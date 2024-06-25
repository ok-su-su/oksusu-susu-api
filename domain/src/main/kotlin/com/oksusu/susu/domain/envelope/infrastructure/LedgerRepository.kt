package com.oksusu.susu.domain.envelope.infrastructure

import com.oksusu.susu.domain.category.domain.QCategoryAssignment
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.common.extension.*
import com.oksusu.susu.domain.envelope.domain.Ledger
import com.oksusu.susu.domain.envelope.domain.QLedger
import com.oksusu.susu.domain.envelope.infrastructure.model.*
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Repository
interface LedgerRepository : JpaRepository<Ledger, Long>, LedgerCustomRepository {
    @Transactional(readOnly = true)
    fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Ledger>

    @Transactional(readOnly = true)
    fun findByIdAndUid(id: Long, uid: Long): Ledger?

    @Transactional(readOnly = true)
    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long

    @Transactional(readOnly = true)
    fun findAllByUidIn(uid: List<Long>) : List<Ledger>
}

interface LedgerCustomRepository {
    @Transactional(readOnly = true)
    fun search(spec: SearchLedgerSpec, pageable: Pageable): Page<SearchLedgerModel>

    @Transactional(readOnly = true)
    fun findLedgerDetail(id: Long, uid: Long): LedgerDetailModel?

    @Transactional(readOnly = true)
    fun countPerCategoryId(): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    fun countPerCategoryIdExceptUid(uid: List<Long>): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    fun countPerCategoryIdByUid(uid: Long): List<CountPerCategoryIdModel>
}

class LedgerCustomRepositoryImpl : LedgerCustomRepository, QuerydslRepositorySupport(Ledger::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qLedger = QLedger.ledger
    private val qCategoryAssignment = QCategoryAssignment.categoryAssignment

    override fun search(spec: SearchLedgerSpec, pageable: Pageable): Page<SearchLedgerModel> {
        val query = JPAQuery<QLedger>(entityManager)
            .select(QSearchLedgerModel(qLedger, qCategoryAssignment))
            .from(qLedger)
            .join(qCategoryAssignment).on(qLedger.id.eq(qCategoryAssignment.targetId))
            .where(
                qLedger.uid.eq(spec.uid),
                qLedger.title.isContains(spec.title),
                qCategoryAssignment.categoryId.isIn(spec.categoryIds),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.LEDGER),
                qLedger.startAt.isGoe(spec.fromStartAt),
                qLedger.startAt.isLoe(spec.toStartAt)
            )

        return querydsl.execute(query, pageable)
    }

    override fun findLedgerDetail(id: Long, uid: Long): LedgerDetailModel? {
        return JPAQuery<QLedger>(entityManager)
            .select(QLedgerDetailModel(qLedger, qCategoryAssignment))
            .from(qLedger)
            .join(qCategoryAssignment).on(qLedger.id.eq(qCategoryAssignment.targetId))
            .where(
                qLedger.id.eq(id),
                qLedger.uid.eq(uid),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.LEDGER)
            ).fetchOne()
    }

    override fun countPerCategoryId(): List<CountPerCategoryIdModel> {
        return JPAQuery<QLedger>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qLedger.id.count()
                )
            )
            .from(qLedger)
            .join(qCategoryAssignment).on(qLedger.id.eq(qCategoryAssignment.targetId))
            .where(
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.LEDGER)
            )
            .groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }

    override fun countPerCategoryIdExceptUid(uid: List<Long>): List<CountPerCategoryIdModel> {
        return JPAQuery<QLedger>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qLedger.id.count()
                )
            )
            .from(qLedger)
            .join(qCategoryAssignment).on(qLedger.id.eq(qCategoryAssignment.targetId))
            .where(
                qLedger.uid.notIn(uid)
            )
            .groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }

    override fun countPerCategoryIdByUid(uid: Long): List<CountPerCategoryIdModel> {
        return JPAQuery<QLedger>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qLedger.id.count()
                )
            )
            .from(qLedger)
            .join(qCategoryAssignment).on(qLedger.id.eq(qCategoryAssignment.targetId))
            .where(
                qLedger.uid.eq(uid)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }
}
