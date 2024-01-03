package com.oksusu.susu.friend.infrastructure

import com.oksusu.susu.extension.execute
import com.oksusu.susu.extension.isEquals
import com.oksusu.susu.friend.domain.Friend
import com.oksusu.susu.friend.domain.QFriend
import com.oksusu.susu.friend.domain.QFriendRelationship
import com.oksusu.susu.friend.infrastructure.model.FriendAndFriendRelationshipModel
import com.oksusu.susu.friend.infrastructure.model.QFriendAndFriendRelationshipModel
import com.oksusu.susu.friend.infrastructure.model.SearchFriendRequestModel
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
interface FriendRepository : JpaRepository<Friend, Long>, FriendCustomRepository {
    @Transactional(readOnly = true)
    fun findByIdAndUid(id: Long, uid: Long): Friend?

    @Transactional(readOnly = true)
    fun existsByUidAndPhoneNumber(uid: Long, phoneNumber: String): Boolean

    @Transactional(readOnly = true)
    fun findAllByIdIn(ids: List<Long>): List<Friend>
}

interface FriendCustomRepository {
    @Transactional(readOnly = true)
    fun search(searchRequest: SearchFriendRequestModel, pageable: Pageable): Page<FriendAndFriendRelationshipModel>
}

class FriendCustomRepositoryImpl : FriendCustomRepository, QuerydslRepositorySupport(Friend::class.java) {
    @Autowired
    @Qualifier("susuEntityManager")
    override fun setEntityManager(entityManager: EntityManager) {
        super.setEntityManager(entityManager)
    }

    private val qFriend = QFriend.friend
    private val qFriendRelationship = QFriendRelationship.friendRelationship

    override fun search(
        searchRequest: SearchFriendRequestModel,
        pageable: Pageable,
    ): Page<FriendAndFriendRelationshipModel> {
        val query = JPAQuery<QFriend>(entityManager)
            .select(QFriendAndFriendRelationshipModel(qFriend, qFriendRelationship))
            .from(qFriend)
            .join(qFriend).on(qFriendRelationship.friendId.eq(qFriend.id))
            .where(
                qFriend.uid.eq(searchRequest.uid),
                qFriend.name.isEquals(searchRequest.name),
                qFriend.phoneNumber.isEquals(searchRequest.phoneNumber)
            )

        return querydsl.execute(query, pageable)
    }
}
