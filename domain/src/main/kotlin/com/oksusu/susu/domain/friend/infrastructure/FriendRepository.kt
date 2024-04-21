package com.oksusu.susu.domain.friend.infrastructure

import com.oksusu.susu.domain.common.extension.execute
import com.oksusu.susu.domain.common.extension.isContains
import com.oksusu.susu.domain.common.extension.isEquals
import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.QFriend
import com.oksusu.susu.domain.friend.domain.QFriendRelationship
import com.oksusu.susu.domain.friend.infrastructure.model.FriendAndFriendRelationshipModel
import com.oksusu.susu.domain.friend.infrastructure.model.QFriendAndFriendRelationshipModel
import com.oksusu.susu.domain.friend.infrastructure.model.SearchFriendSpec
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
interface FriendRepository : JpaRepository<Friend, Long>, FriendCustomRepository {
    @Transactional(readOnly = true)
    fun findByIdAndUid(id: Long, uid: Long): Friend?

    @Transactional(readOnly = true)
    fun existsByUidAndPhoneNumber(uid: Long, phoneNumber: String): Boolean

    @Transactional(readOnly = true)
    fun findAllByIdIn(ids: List<Long>): List<Friend>

    @Transactional(readOnly = true)
    fun findAllByUidAndIdIn(uid: Long, ids: List<Long>): List<Friend>

    @Transactional(readOnly = true)
    fun countByCreatedAtBetween(startAt: LocalDateTime, endAt: LocalDateTime): Long
}

interface FriendCustomRepository {
    @Transactional(readOnly = true)
    fun search(spec: SearchFriendSpec, pageable: Pageable): Page<FriendAndFriendRelationshipModel>
}

class FriendCustomRepositoryImpl : FriendCustomRepository, QuerydslRepositorySupport(Friend::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qFriend = QFriend.friend
    private val qFriendRelationship = QFriendRelationship.friendRelationship

    override fun search(spec: SearchFriendSpec, pageable: Pageable): Page<FriendAndFriendRelationshipModel> {
        val query = JPAQuery<QFriend>(entityManager)
            .select(QFriendAndFriendRelationshipModel(qFriend, qFriendRelationship))
            .from(qFriend)
            .join(qFriendRelationship).on(qFriend.id.eq(qFriendRelationship.friendId))
            .where(
                qFriend.uid.eq(spec.uid),
                qFriend.name.isContains(spec.name),
                qFriend.phoneNumber.isEquals(spec.phoneNumber)
            )

        return querydsl.execute(query, pageable)
    }
}
