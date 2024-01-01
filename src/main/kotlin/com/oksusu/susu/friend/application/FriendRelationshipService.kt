package com.oksusu.susu.friend.application

import com.oksusu.susu.friend.domain.FriendRelationship
import com.oksusu.susu.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.friend.infrastructure.model.CountPerRelationshipIdModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    suspend fun countPerRelationshipId(): List<CountPerRelationshipIdModel> {
        return withContext(Dispatchers.IO) {
            friendRelationshipRepository.countPerRelationshipId()
        }
    }

    suspend fun countPerRelationshipIdByUid(uid: Long): List<CountPerRelationshipIdModel> {
        return withContext(Dispatchers.IO) {
            friendRelationshipRepository.countPerRelationshipIdByUid(uid)
        }
    }
}
