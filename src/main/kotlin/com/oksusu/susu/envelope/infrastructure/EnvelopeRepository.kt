package com.oksusu.susu.envelope.infrastructure

import com.oksusu.susu.category.domain.QCategoryAssignment
import com.oksusu.susu.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.envelope.domain.Envelope
import com.oksusu.susu.envelope.domain.QEnvelope
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.*
import com.oksusu.susu.extension.execute
import com.oksusu.susu.extension.isEquals
import com.oksusu.susu.friend.domain.QFriend
import com.oksusu.susu.friend.domain.QFriendRelationship
import com.oksusu.susu.user.domain.QUser
import com.querydsl.core.types.dsl.CaseBuilder
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
    suspend fun getMaxAmountByUid(uid: Long, type: EnvelopeType): Long?

    @Transactional(readOnly = true)
    suspend fun findEnvelopeAndFriendByUid(maxAmount: Long, uid: Long, type: EnvelopeType): EnvelopeAndFriendModel?

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
    fun search(spec: SearchEnvelopeSpec, pageable: Pageable): Page<SearchEnvelopeModel>

    @Transactional(readOnly = true)
    fun findFriendStatistics(sepc: SearchFriendStatisticsSpec, pageable: Pageable): Page<FriendStatisticsModel>
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

    override suspend fun getMaxAmountByUid(uid: Long, type: EnvelopeType): Long? {
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

    override suspend fun findEnvelopeAndFriendByUid(
        maxAmount: Long,
        uid: Long,
        type: EnvelopeType,
    ): EnvelopeAndFriendModel? {
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
                qEnvelope.type.eq(type),
                qEnvelope.handedOverAt.between(from, to),
                qCategoryAssignment.targetType.eq(CategoryAssignmentType.ENVELOPE)
            ).groupBy(qEnvelope.handedOverAt.month())
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

    override fun search(spec: SearchEnvelopeSpec, pageable: Pageable): Page<SearchEnvelopeModel> {
        /** select */
        val query = JPAQuery<Envelope>(entityManager)
            .select(
                QSearchEnvelopeModel(
                    qEnvelope,
                    if (spec.include.contains(IncludeSpec.FRIEND)) qFriend else null,
                    if (spec.include.contains(IncludeSpec.RELATION)) qFriendRelationship else null,
                    if (spec.include.contains(IncludeSpec.CATEGORY)) qCategoryAssignment else null
                )
            ).from(qEnvelope)

        /** join */
        if (spec.include.contains(IncludeSpec.FRIEND)) {
            query.join(qFriend).on(qEnvelope.friendId.eq(qFriend.id))
        }
        if (spec.include.contains(IncludeSpec.RELATION)) {
            query.join(qFriendRelationship).on(qEnvelope.friendId.eq(qFriendRelationship.friendId))
        }
        if (spec.include.contains(IncludeSpec.CATEGORY)) {
            query.join(qCategoryAssignment).on(qEnvelope.id.eq(qCategoryAssignment.targetId))
        }

        /** where */
        query.where(
            qEnvelope.uid.eq(spec.uid),
            qEnvelope.ledgerId.isEquals(spec.ledgerId)
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

        val query = JPAQuery<Envelope>(entityManager)
            .select(
                QFriendStatisticsModel(
                    qEnvelope.friendId,
                    sentAmount.sum(),
                    receivedAmount.sum()
                )
            )
            .from(QEnvelope.envelope)
            .where(qEnvelope.uid.eq(spec.uid))

        if (!spec.friendIds.isNullOrEmpty()) {
            query
                .where(qEnvelope.friendId.`in`(spec.friendIds))
        }

        val totalAmount = sentAmount.sum().add(receivedAmount.sum())
        if (spec.fromTotalAmounts != null) {
            query.where(totalAmount.goe(totalAmount))
        }
        if (spec.toTotalAmounts != null) {
            query.where(totalAmount.loe(totalAmount))
        }

        query.groupBy(qEnvelope.friendId)

        return querydsl.execute(query, pageable)
    }
}
