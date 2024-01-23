package com.oksusu.susu.ledger.infrastructure

import com.oksusu.susu.category.domain.QCategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.envelope.infrastructure.model.QCountPerCategoryIdModel
import com.oksusu.susu.extension.execute
import com.oksusu.susu.extension.isContains
import com.oksusu.susu.extension.isIn
import com.oksusu.susu.ledger.domain.Ledger
import com.oksusu.susu.ledger.domain.QLedger
import com.oksusu.susu.ledger.infrastructure.model.LedgerDetailModel
import com.oksusu.susu.ledger.infrastructure.model.QLedgerDetailModel
import com.oksusu.susu.ledger.infrastructure.model.QSearchLedgerModel
import com.oksusu.susu.ledger.infrastructure.model.SearchLedgerModel
import com.oksusu.susu.ledger.infrastructure.model.SearchLedgerSpec
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

@Repository
interface LedgerRepository : JpaRepository<Ledger, Long>, LedgerCustomRepository {
    @Transactional(readOnly = true)
    fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Ledger>

    @Transactional(readOnly = true)
    fun findByIdAndUid(id: Long, uid: Long): Ledger?
}

interface LedgerCustomRepository {
    @Transactional(readOnly = true)
    fun search(spec: SearchLedgerSpec, pageable: Pageable): Page<SearchLedgerModel>

    @Transactional(readOnly = true)
    fun findLedgerDetail(id: Long, uid: Long): LedgerDetailModel?

    @Transactional(readOnly = true)
    fun countPerCategoryId(): List<CountPerCategoryIdModel>

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
                spec.fromStartAt?.let { fromStartAt -> qLedger.startAt.after(fromStartAt) },
                spec.toStartAt?.let { toStartAt -> qLedger.startAt.before(toStartAt) }
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
            ).groupBy(qCategoryAssignment.categoryId)
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
                qLedger.uid.eq(uid),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.LEDGER)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }
}
