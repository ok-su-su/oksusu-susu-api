package com.oksusu.susu.domain.envelope.infrastructure

import com.oksusu.susu.domain.category.domain.QCategoryAssignment
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.common.extension.*
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.envelope.domain.QEnvelope
import com.oksusu.susu.domain.envelope.domain.QLedger
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.model.*
import com.oksusu.susu.domain.friend.domain.QFriend
import com.oksusu.susu.domain.friend.domain.QFriendRelationship
import com.oksusu.susu.domain.user.domain.QUser
import com.querydsl.core.types.dsl.CaseBuilder
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Slice
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

    @Transactional(readOnly = true)
    fun findTop1ByUidAndTypeOrderByAmountDesc(uid: Long, type: EnvelopeType): Envelope?

    @Transactional(readOnly = true)
    fun findTop1ByUidAndTypeOrderByAmountAsc(uid: Long, type: EnvelopeType): Envelope?

    @Transactional(readOnly = true)
    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long

    @Transactional
    fun deleteAllByFriendIdIn(friendIds: List<Long>)

    @Transactional(readOnly = true)
    fun countByUidAndFriendId(uid: Long, friendId: Long): Long

    @Transactional(readOnly = true)
    fun findAllByUidIn(uid: List<Long>): List<Envelope>
}

interface EnvelopeCustomRepository {
    @Transactional(readOnly = true)
    fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel>

    @Transactional(readOnly = true)
    fun countTotalAmountAndCount(ledgerId: Long): CountTotalAmountsAndCountsModel

    @Transactional(readOnly = true)
    fun findDetailEnvelope(id: Long, uid: Long): EnvelopeDetailModel?

    @Transactional(readOnly = true)
    fun getCuttingTotalAmountPerHandedOverAtBetweenExceptUid(
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
        minAmount: Long,
        maxAmount: Long,
        uid: List<Long>,
    ): List<CountPerHandedOverAtModel>

    @Transactional(readOnly = true)
    fun getTotalAmountPerHandedOverAtBetweenByUid(
        uid: Long,
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel>

    @Transactional(readOnly = true)
    fun countPerCategoryId(): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    fun countPerCategoryIdExceptUid(uid: List<Long>): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    fun countPerCategoryIdExceptUidByCreatedAtAfter(
        uid: List<Long>,
        targetDate: LocalDateTime,
    ): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    fun countPerCategoryIdByUid(uid: Long): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    fun getCuttingTotalAmountPerStatisticGroupExceptUid(
        min: Long,
        max: Long,
        uid: List<Long>,
    ): List<CountAvgAmountPerStatisticGroupModel>

    @Transactional(readOnly = true)
    fun getCuttingTotalAmountPerStatisticGroupExceptUidByCreatedAtAfter(
        min: Long,
        max: Long,
        uid: List<Long>,
        targetDate: LocalDateTime,
    ): List<CountAvgAmountPerStatisticGroupModel>

    @Transactional(readOnly = true)
    fun search(spec: SearchEnvelopeSpec, pageable: Pageable): Page<Envelope>

    @Transactional(readOnly = true)
    fun findFriendStatistics(spec: SearchFriendStatisticsSpec, pageable: Pageable): Page<FriendStatisticsModel>

    @Transactional(readOnly = true)
    fun findAllDetailEnvelopeAndLedgerByEnvelopeType(
        uid: Long,
        envelopeType: EnvelopeType,
        pageable: Pageable,
    ): Slice<EnvelopeDetailAndLedgerModel>

    @Transactional(readOnly = true)
    fun findLatestFriendEnvelopes(friendIds: Set<Long>): List<Envelope>

    @Transactional(readOnly = true)
    fun getMaxAmountEnvelopeInfoByUid(uid: Long, type: EnvelopeType): EnvelopeAndFriendModel?

    @Transactional(readOnly = true)
    fun countTotalAmountByUid(uid: Long): Long?

    @Transactional(readOnly = true)
    fun getUserCountHadEnvelope(): Long

    @Transactional(readOnly = true)
    fun getEnvelopeAmountByPositionOrderByAmountExceptUid(position: Long, uid: List<Long>): Long

    @Transactional(readOnly = true)
    fun countExceptUid(uid: List<Long>): Long
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
    private val qUser = QUser.user
    private val qLedger = QLedger.ledger

    override fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel> {
        return JPAQuery<QEnvelope>(entityManager)
            .select(
                QCountTotalAmountsAndCountsModel(
                    qEnvelope.ledgerId,

                    CaseBuilder()
                        .`when`(QEnvelope.envelope.type.eq(EnvelopeType.SENT))
                        .then(QEnvelope.envelope.amount)
                        .otherwise(0)
                        .sum(),

                    CaseBuilder()
                        .`when`(QEnvelope.envelope.type.eq(EnvelopeType.RECEIVED))
                        .then(QEnvelope.envelope.amount)
                        .otherwise(0)
                        .sum(),

                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .where(qEnvelope.ledgerId.`in`(ledgerIds))
            .groupBy(qEnvelope.ledgerId)
            .fetch()
    }

    override fun countTotalAmountAndCount(ledgerId: Long): CountTotalAmountsAndCountsModel {
        return JPAQuery<QEnvelope>(entityManager)
            .select(
                QCountTotalAmountsAndCountsModel(
                    qEnvelope.ledgerId,

                    CaseBuilder()
                        .`when`(QEnvelope.envelope.type.eq(EnvelopeType.SENT))
                        .then(QEnvelope.envelope.amount)
                        .otherwise(0)
                        .sum(),

                    CaseBuilder()
                        .`when`(QEnvelope.envelope.type.eq(EnvelopeType.RECEIVED))
                        .then(QEnvelope.envelope.amount)
                        .otherwise(0)
                        .sum(),

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

    override fun getCuttingTotalAmountPerHandedOverAtBetweenExceptUid(
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
        minAmount: Long,
        maxAmount: Long,
        uid: List<Long>,
    ): List<CountPerHandedOverAtModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerHandedOverAtModel(
                    qEnvelope.handedOverAt.yearMonth(),
                    qEnvelope.amount.sum()
                )
            ).from(qEnvelope)
            .where(
                qEnvelope.type.eq(type),
                qEnvelope.handedOverAt.between(from, to),
                qEnvelope.amount.between(minAmount, maxAmount),
                qEnvelope.uid.notIn(uid)
            ).groupBy(qEnvelope.handedOverAt.yearMonth())
            .fetch()
    }

    override fun getTotalAmountPerHandedOverAtBetweenByUid(
        uid: Long,
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerHandedOverAtModel(
                    qEnvelope.handedOverAt.yearMonth(),
                    qEnvelope.amount.sum()
                )
            ).from(qEnvelope)
            .where(
                qEnvelope.uid.eq(uid),
                qEnvelope.type.eq(type),
                qEnvelope.handedOverAt.between(from, to)
            ).groupBy(qEnvelope.handedOverAt.yearMonth())
            .fetch()
    }

    override fun countPerCategoryId(): List<CountPerCategoryIdModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.ledgerId.isNull,
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }

    override fun countPerCategoryIdExceptUid(uid: List<Long>): List<CountPerCategoryIdModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE),
                qEnvelope.ledgerId.isNull,
                qEnvelope.uid.notIn(uid)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }

    override fun countPerCategoryIdExceptUidByCreatedAtAfter(
        uid: List<Long>,
        targetDate: LocalDateTime,
    ): List<CountPerCategoryIdModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE),
                qEnvelope.ledgerId.isNull,
                qEnvelope.uid.notIn(uid),
                qEnvelope.createdAt.after(targetDate)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }

    override fun countPerCategoryIdByUid(uid: Long): List<CountPerCategoryIdModel> {
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
                qEnvelope.ledgerId.isNull,
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }

    override fun getCuttingTotalAmountPerStatisticGroupExceptUid(
        minAmount: Long,
        maxAmount: Long,
        uid: List<Long>,
    ): List<CountAvgAmountPerStatisticGroupModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountAvgAmountPerStatisticGroupModel(
                    qCategoryAssignment.categoryId,
                    qFriendRelationship.relationshipId,
                    qUser.birth.year().castToNum(Long::class.java),
                    qEnvelope.amount.sum(),
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qUser).on(qEnvelope.uid.eq(qUser.id))
            .join(qFriendRelationship).on(qEnvelope.friendId.eq(qFriendRelationship.friendId))
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.amount.between(minAmount, maxAmount),
                qEnvelope.uid.notIn(uid)
            )
            .groupBy(
                qCategoryAssignment.categoryId,
                qUser.birth.year().castToNum(Long::class.java),
                qFriendRelationship.relationshipId
            ).fetch()
    }

    override fun getCuttingTotalAmountPerStatisticGroupExceptUidByCreatedAtAfter(
        minAmount: Long,
        maxAmount: Long,
        uid: List<Long>,
        targetDate: LocalDateTime,
    ): List<CountAvgAmountPerStatisticGroupModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountAvgAmountPerStatisticGroupModel(
                    qCategoryAssignment.categoryId,
                    qFriendRelationship.relationshipId,
                    qUser.birth.year().castToNum(Long::class.java),
                    qEnvelope.amount.sum(),
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qUser).on(qEnvelope.uid.eq(qUser.id))
            .join(qFriendRelationship).on(qEnvelope.friendId.eq(qFriendRelationship.friendId))
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.amount.between(minAmount, maxAmount),
                qEnvelope.uid.notIn(uid),
                qEnvelope.createdAt.after(targetDate)
            )
            .groupBy(
                qCategoryAssignment.categoryId,
                qUser.birth.year().castToNum(Long::class.java),
                qFriendRelationship.relationshipId
            ).fetch()
    }

    override fun search(spec: SearchEnvelopeSpec, pageable: Pageable): Page<Envelope> {
        /** select */
        val query = JPAQuery<Envelope>(entityManager)
            .select(qEnvelope)
            .from(qEnvelope)

        /** where */
        query.where(
            qEnvelope.uid.eq(spec.uid),
            qEnvelope.friendId.isIn(spec.friendId),
            qEnvelope.ledgerId.isEquals(spec.ledgerId),
            qEnvelope.type.isIn(spec.types),
            qEnvelope.amount.isGoe(spec.fromAmount),
            qEnvelope.amount.isLoe(spec.toAmount)
        )

        return querydsl.execute(query, pageable)
    }

    override fun findFriendStatistics(
        spec: SearchFriendStatisticsSpec,
        pageable: Pageable,
    ): Page<FriendStatisticsModel> {
        val sentAmount = CaseBuilder()
            .`when`(QEnvelope.envelope.type.eq(EnvelopeType.SENT))
            .then(QEnvelope.envelope.amount)
            .otherwise(0)
            .sum()

        val receivedAmount = CaseBuilder()
            .`when`(QEnvelope.envelope.type.eq(EnvelopeType.RECEIVED))
            .then(QEnvelope.envelope.amount)
            .otherwise(0)
            .sum()

        val totalAmount = sentAmount.add(receivedAmount)

        val query = JPAQuery<Envelope>(entityManager)
            .select(
                QFriendStatisticsModel(
                    qEnvelope.friendId,
                    sentAmount,
                    receivedAmount,
                    qEnvelope.handedOverAt
                )
            )
            .from(QEnvelope.envelope)
            .where(
                qEnvelope.uid.eq(spec.uid),
                qEnvelope.friendId.isIn(spec.friendIds)
            ).groupBy(qEnvelope.friendId)
            .having(
                totalAmount.isGoe(spec.fromTotalAmounts),
                totalAmount.isLoe(spec.toTotalAmounts)
            )

        return querydsl.execute(query, pageable)
    }

    override fun findAllDetailEnvelopeAndLedgerByEnvelopeType(
        uid: Long,
        envelopeType: EnvelopeType,
        pageable: Pageable,
    ): Slice<EnvelopeDetailAndLedgerModel> {
        val query = JPAQuery<Envelope>(entityManager)
            .select(
                QEnvelopeDetailAndLedgerModel(
                    qLedger,
                    qEnvelope,
                    qFriend,
                    qFriendRelationship,
                    qCategoryAssignment
                )
            ).from(qEnvelope)
            .join(qFriend).on(qEnvelope.friendId.eq(qFriend.id))
            .join(qFriendRelationship).on(qEnvelope.friendId.eq(qFriendRelationship.friendId))
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .leftJoin(qLedger).on(qEnvelope.ledgerId.eq(qLedger.id))
            .where(
                qEnvelope.uid.eq(uid),
                qEnvelope.type.eq(envelopeType),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            )

        return querydsl.executeSlice(query, pageable)
    }

    override fun findLatestFriendEnvelopes(friendIds: Set<Long>): List<Envelope> {
        return JPAQuery<Envelope>(entityManager)
            .select(qEnvelope)
            .from(qEnvelope)
            .where(qEnvelope.friendId.`in`(friendIds))
            .groupBy(qEnvelope.friendId)
            .having(qEnvelope.handedOverAt.eq(qEnvelope.handedOverAt.max()))
            .fetch()
    }

    override fun getMaxAmountEnvelopeInfoByUid(uid: Long, type: EnvelopeType): EnvelopeAndFriendModel? {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QEnvelopeAndFriendModel(
                    qEnvelope,
                    qFriend
                )
            ).from(qEnvelope)
            .join(qFriend).on(qEnvelope.friendId.eq(qFriend.id))
            .where(
                qEnvelope.uid.eq(uid),
                qEnvelope.type.eq(type)
            ).orderBy(qEnvelope.amount.desc())
            .fetchFirst()
    }

    override fun countTotalAmountByUid(uid: Long): Long? {
        return JPAQuery<Envelope>(entityManager)
            .select(qEnvelope.amount.sum())
            .from(qEnvelope)
            .where(qEnvelope.uid.eq(uid))
            .fetchOne()
    }

    override fun getUserCountHadEnvelope(): Long {
        return JPAQuery<Envelope>(entityManager)
            .select(qEnvelope.uid.countDistinct())
            .from(qEnvelope)
            .fetchFirst()
    }

    override fun getEnvelopeAmountByPositionOrderByAmountExceptUid(position: Long, uid: List<Long>): Long {
        return JPAQuery<Envelope>(entityManager)
            .select(qEnvelope.amount)
            .from(qEnvelope)
            .where(qEnvelope.uid.notIn(uid))
            .orderBy(qEnvelope.amount.asc())
            .offset(position)
            .limit(1)
            .fetchFirst()
    }

    override fun countExceptUid(uid: List<Long>): Long {
        return JPAQuery<Envelope>(entityManager)
            .select(qEnvelope.count())
            .from(qEnvelope)
            .where(qEnvelope.uid.notIn(uid))
            .fetchFirst()
    }
}
