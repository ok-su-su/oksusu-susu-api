package com.oksusu.susu.envelope.infrastructure

import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.QEnvelope
import com.oksusu.susu.envelope.infrastructure.model.CountTotalAmountsAndCountsModel
import com.oksusu.susu.envelope.infrastructure.model.QCountTotalAmountsAndCountsModel
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface EnvelopeRepository : JpaRepository<Envelope, Long>, EnvelopeCustomRepository

interface EnvelopeCustomRepository {
    @Transactional(readOnly = true)
    fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel>
}

class EnvelopeCustomRepositoryImpl : EnvelopeCustomRepository, QuerydslRepositorySupport(Envelope::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qEnvelope = QEnvelope.envelope

    override fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel> {
        return JPAQuery<QEnvelope>(entityManager)
            .select(
                QCountTotalAmountsAndCountsModel(
                    qEnvelope.ledgerId,
                    qEnvelope.amount.sum(),
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .where(
                qEnvelope.ledgerId.`in`(ledgerIds)
            ).groupBy(qEnvelope.ledgerId)
            .fetch()
    }
}
