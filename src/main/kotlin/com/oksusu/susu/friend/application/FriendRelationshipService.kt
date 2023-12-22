package com.oksusu.susu.friend.application

import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.friend.infrastructure.FriendRelationshipRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FriendRelationshipService(
    private val friendRelationshipRepository: FriendRelationshipRepository,
) {
    @Transactional
    fun saveSync(friendRelationship: FriendRelationship): FriendRelationship {
        return friendRelationshipRepository.save(friendRelationship)
    }
}
