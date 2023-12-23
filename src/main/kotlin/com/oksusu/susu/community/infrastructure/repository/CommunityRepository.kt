package com.oksusu.susu.community.infrastructure.repository

import com.oksusu.susu.community.domain.Community
import com.oksusu.susu.community.domain.vo.CommunityType
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
import org.springframework.data.domain.Slice
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.support.QuerydslRepositorySupport
import org.springframework.stereotype.Repository
import org.springframework.transaction.annotation.Transactional

@Repository
interface CommunityRepository : JpaRepository<Community, Long> {
    fun findAllByIsActiveAndTypeOrderByCreatedAtDesc(b: Boolean, vote: CommunityType, toDefault: Pageable): Slice<Community>
}