package com.oksusu.susu.envelope.infrastructure

import com.oksusu.susu.category.domain.QCategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.QEnvelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.*
import com.oksusu.susu.envelope.infrastructure.model.CountPerHandedOverAtModel
import com.oksusu.susu.friend.domain.QFriend
import com.oksusu.susu.friend.domain.QFriendRelationship
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
interface EnvelopeRepository : JpaRepository<Envelope, Long>, EnvelopeCustomRepository {
    @Transactional(readOnly = true)
    fun findByIdAndUid(id: Long, uid: Long): Envelope?

    @Transactional
    fun deleteAllByLedgerId(ledgerId: Long)

    @Transactional(readOnly = true)
    fun findAllByLedgerId(ledgerId: Long): List<Envelope>
}

interface EnvelopeCustomRepository {
    @Transactional(readOnly = true)
    fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel>

    @Transactional(readOnly = true)
    fun countTotalAmountAndCount(ledgerId: Long): CountTotalAmountsAndCountsModel

    @Transactional(readOnly = true)
    fun findDetailEnvelope(id: Long, uid: Long): EnvelopeDetailModel?

    @Transactional(readOnly = true)
    suspend fun getMaxAmount(uid: Long, type: EnvelopeType): Long?

    @Transactional(readOnly = true)
    suspend fun findEnvelopeAndFriend(maxAmount: Long, uid: Long, type: EnvelopeType): EnvelopeAndFriendModel?

    @Transactional(readOnly = true)
    suspend fun countPerHandedOverAtBetween(
        uid: Long,
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel>

    @Transactional(readOnly = true)
    suspend fun countPerCategoryId(uid: Long): List<CountPerCategoryIdModel>
}

class EnvelopeCustomRepositoryImpl : EnvelopeCustomRepository, QuerydslRepositorySupport(Envelope::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qEnvelope = QEnvelope.envelope
    private val qFriend = QFriend.friend
    private val qFriendRelationship = QFriendRelationship.friendRelationship
    private val qCategoryAssignment = QCategoryAssignment.categoryAssignment

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

    override fun countTotalAmountAndCount(ledgerId: Long): CountTotalAmountsAndCountsModel {
        return JPAQuery<QEnvelope>(entityManager)
            .select(
                QCountTotalAmountsAndCountsModel(
                    qEnvelope.ledgerId,
                    qEnvelope.amount.sum(),
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .where(qEnvelope.ledgerId.eq(ledgerId))
            .fetchFirst()
    }

    override fun findDetailEnvelope(id: Long, uid: Long): EnvelopeDetailModel? {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QEnvelopeDetailModel(
                    qEnvelope,
                    qFriend,
                    qFriendRelationship,
                    qCategoryAssignment
                )
            ).from(qEnvelope)
            .join(qFriend).on(qEnvelope.friendId.eq(qFriend.id))
            .join(qFriendRelationship).on(qEnvelope.friendId.eq(qFriendRelationship.friendId))
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.id.eq(id),
                qEnvelope.uid.eq(uid),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).fetchFirst()
    }

    override suspend fun getMaxAmount(uid: Long, type: EnvelopeType): Long? {
        return JPAQuery<Envelope>(entityManager)
            .select(
                qEnvelope.amount.max()
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.uid.eq(uid),
                qEnvelope.type.eq(type),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).fetchFirst()
    }

    override suspend fun findEnvelopeAndFriend(maxAmount: Long, uid: Long, type: EnvelopeType): EnvelopeAndFriendModel? {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QEnvelopeAndFriendModel(
                    qEnvelope,
                    qFriend
                )
            ).from(qEnvelope)
            .join(qFriend).on(qEnvelope.friendId.eq(qFriend.id))
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.uid.eq(uid),
                qEnvelope.type.eq(type),
                qEnvelope.amount.eq(maxAmount),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).fetchFirst()
    }

    override suspend fun countPerHandedOverAtBetween(
        uid: Long,
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerHandedOverAtModel(
                    qEnvelope.handedOverAt.month(),
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.uid.eq(uid),
                qEnvelope.type.eq(type),
                qEnvelope.handedOverAt.between(from, to),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).groupBy(qEnvelope.handedOverAt.month())
            .fetch()
    }

    override suspend fun countPerCategoryId(uid: Long): List<CountPerCategoryIdModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.uid.eq(uid),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }
}
