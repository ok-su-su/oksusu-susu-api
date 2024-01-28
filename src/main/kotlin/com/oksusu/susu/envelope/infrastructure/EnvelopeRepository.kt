package com.oksusu.susu.envelope.infrastructure

import com.oksusu.susu.category.domain.QCategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.QEnvelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.*
import com.oksusu.susu.extension.execute
import com.oksusu.susu.extension.executeSlice
import com.oksusu.susu.extension.isEquals
import com.oksusu.susu.extension.isGoe
import com.oksusu.susu.extension.isIn
import com.oksusu.susu.extension.isLoe
import com.oksusu.susu.friend.domain.QFriend
import com.oksusu.susu.friend.domain.QFriendRelationship
import com.oksusu.susu.ledger.domain.QLedger
import com.oksusu.susu.user.domain.QUser
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
    fun findTop1ByUidAndTypeOrderByAmount(uid: Long, type: EnvelopeType): Envelope?
}

interface EnvelopeCustomRepository {
    @Transactional(readOnly = true)
    fun countTotalAmountsAndCounts(ledgerIds: List<Long>): List<CountTotalAmountsAndCountsModel>

    @Transactional(readOnly = true)
    fun countTotalAmountAndCount(ledgerId: Long): CountTotalAmountsAndCountsModel

    @Transactional(readOnly = true)
    fun findDetailEnvelope(id: Long, uid: Long): EnvelopeDetailModel?

    @Transactional(readOnly = true)
    suspend fun countPerHandedOverAtBetween(
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel>

    @Transactional(readOnly = true)
    suspend fun countPerHandedOverAtBetweenByUid(
        uid: Long,
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel>

    @Transactional(readOnly = true)
    suspend fun countPerCategoryId(): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    suspend fun countPerCategoryIdByUid(uid: Long): List<CountPerCategoryIdModel>

    @Transactional(readOnly = true)
    suspend fun countAvgAmountPerStatisticGroup(): List<CountAvgAmountPerStatisticGroupModel>

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
    fun getMaxAmountEnvelopeInfoByUid(uid: Long, type: EnvelopeType): EnvelopeAndFriendModel
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

    override suspend fun countPerHandedOverAtBetween(
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerHandedOverAtModel(
                    qEnvelope.handedOverAt.yearMonth(),
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qEnvelope.type.eq(type),
                qEnvelope.handedOverAt.between(from, to),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).groupBy(qEnvelope.handedOverAt.yearMonth())
            .fetch()
    }

    override suspend fun countPerHandedOverAtBetweenByUid(
        uid: Long,
        type: EnvelopeType,
        from: LocalDateTime,
        to: LocalDateTime,
    ): List<CountPerHandedOverAtModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerHandedOverAtModel(
                    qEnvelope.handedOverAt.yearMonth(),
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .where(
                qEnvelope.uid.eq(uid),
                qEnvelope.type.eq(type),
                qEnvelope.handedOverAt.between(from, to),
            ).groupBy(qEnvelope.handedOverAt.yearMonth())
            .fetch()
    }

    override suspend fun countPerCategoryId(): List<CountPerCategoryIdModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountPerCategoryIdModel(
                    qCategoryAssignment.categoryId,
                    qEnvelope.id.count()
                )
            ).from(qEnvelope)
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .where(
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).groupBy(qCategoryAssignment.categoryId)
            .fetch()
    }

    override suspend fun countPerCategoryIdByUid(uid: Long): List<CountPerCategoryIdModel> {
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

    override suspend fun countAvgAmountPerStatisticGroup(): List<CountAvgAmountPerStatisticGroupModel> {
        return JPAQuery<Envelope>(entityManager)
            .select(
                QCountAvgAmountPerStatisticGroupModel(
                    qCategoryAssignment.categoryId,
                    qFriendRelationship.relationshipId,
                    qUser.birth.year().castToNum(Long::class.java),
                    qEnvelope.amount.avg().castToNum(Long::class.java)
                )
            ).from(qEnvelope)
            .join(qFriendRelationship).on(qEnvelope.friendId.eq(qFriendRelationship.friendId))
            .join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
            .join(qUser).on(qEnvelope.uid.eq(qUser.id))
            .where(
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            )
            .groupBy(
                qCategoryAssignment.categoryId,
                qFriendRelationship.relationshipId,
                qUser.birth.year().castToNum(Long::class.java)
            )
            .fetch()
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

        val receivedAmount = CaseBuilder()
            .`when`(QEnvelope.envelope.type.eq(EnvelopeType.RECEIVED))
            .then(QEnvelope.envelope.amount)
            .otherwise(0)

        val totalAmount = sentAmount.sum().add(receivedAmount.sum())

        val query = JPAQuery<Envelope>(entityManager)
            .select(
                QFriendStatisticsModel(
                    qEnvelope.friendId,
                    sentAmount.sum(),
                    receivedAmount.sum()
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

    override fun getMaxAmountEnvelopeInfoByUid(uid: Long, type: EnvelopeType): EnvelopeAndFriendModel {
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
}
