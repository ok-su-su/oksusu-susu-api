package com.oksusu.susu.domain.friend.infrastructure

import com.oksusu.susu.domain.envelope.domain.QEnvelope
import com.oksusu.susu.domain.friend.domain.*
import com.oksusu.susu.domain.friend.infrastructure.model.CountPerRelationshipIdModel
import com.oksusu.susu.domain.friend.infrastructure.model.QCountPerRelationshipIdModel
import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.FriendRelationship
import com.querydsl.jpa.impl.JPAQuery
import jakarta.persistence.EntityManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface FriendRelationshipRepository : JpaRepository<FriendRelationship, Long>, FriendRelationshipCustomRepository {
    @Transactional(readOnly = true)
    fun findAllByFriendIdIn(friendIds: List<Long>): List<FriendRelationship>

    @Transactional(readOnly = true)
    fun findByFriendId(friendId: Long): FriendRelationship?

    @Transactional
    fun deleteByFriendIdIn(friendIds: List<Long>)

    @Transactional
    fun deleteByFriendId(friendId: Long)
}

interface FriendRelationshipCustomRepository {
    @Transactional(readOnly = true)
    suspend fun countPerRelationshipId(): List<CountPerRelationshipIdModel>

    @Transactional(readOnly = true)
    suspend fun countPerRelationshipIdByUid(uid: Long): List<CountPerRelationshipIdModel>
}

class FriendRelationshipCustomRepositoryImpl : FriendRelationshipCustomRepository, QuerydslRepositorySupport(
    Friend::class.java
) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qEnvelope = QEnvelope.envelope
    private val qFriend = QFriend.friend
    private val qFriendRelationship = QFriendRelationship.friendRelationship
    private val qRelationShip = QRelationship.relationship

    override suspend fun countPerRelationshipId(): List<CountPerRelationshipIdModel> {
        return JPAQuery<FriendRelationship>(entityManager)
            .select(
                QCountPerRelationshipIdModel(qFriendRelationship.relationshipId, qEnvelope.id.count())
            ).from(qFriendRelationship)
            .join(qEnvelope).on(qFriendRelationship.friendId.eq(qEnvelope.friendId))
            .groupBy(qFriendRelationship.relationshipId)
            .fetch()
    }

    override suspend fun countPerRelationshipIdByUid(uid: Long): List<CountPerRelationshipIdModel> {
        return JPAQuery<FriendRelationship>(entityManager)
            .select(
                QCountPerRelationshipIdModel(qFriendRelationship.relationshipId, qEnvelope.id.count())
            ).from(qFriendRelationship)
            .join(qEnvelope).on(qFriendRelationship.friendId.eq(qEnvelope.friendId))
            .where(
                qEnvelope.uid.eq(uid)
            ).groupBy(qFriendRelationship.relationshipId)
            .fetch()
    }
}
