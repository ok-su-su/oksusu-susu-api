package com.oksusu.susu.batch.user.job

import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.FriendRepository
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import com.oksusu.susu.domain.user.infrastructure.UserStatusHistoryRepository
import com.oksusu.susu.domain.user.infrastructure.UserStatusTypeRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class DeleteWithdrawUserDataJob(
    val userStatusTypeRepository: UserStatusTypeRepository,
    val userStatusHistoryRepository: UserStatusHistoryRepository,
    val envelopeRepository: EnvelopeRepository,
    val ledgerRepository: LedgerRepository,
    val friendRepository: FriendRepository,
    val friendRelationshipRepository: FriendRelationshipRepository,
    val postRepository: PostRepository,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val DELETE_BEFORE_DAYS = 1L
        private const val DELETE_CHUNK = 1000
    }

    suspend fun deleteWithdrawUserData() {
        logger.info { "start delete withdraw user data" }

        val deletedUserStatusId = getDeletedUserStatusId()
        val targetDate = LocalDateTime.now().minusDays(DELETE_BEFORE_DAYS)

        /** 삭제된 uid */
        val deletedUids = getDeletedUids(deletedUserStatusId, targetDate)

        coroutineScope {
            /** 삭제 유저의 봉투 삭제 */
            val deleteEnvelopesDeferred = async { deleteEnvelopes(deletedUids) }

            /** 삭제 유저의 장부 삭제 */
            val deleteLedgersDeferred = async { deleteLedgers(deletedUids) }

            /** 삭제 유저의 친구 및 친구 관계 삭제 */
            val deleteFriendsDeferred = async { deleteFriends(deletedUids) }

            /** 삭제 유저의 게시물 삭제 */
            val deletePostsDeferred = async { deletePosts(deletedUids) }

            awaitAll(deleteEnvelopesDeferred, deleteLedgersDeferred, deleteFriendsDeferred, deletePostsDeferred)
        }

        logger.info { "finish delete withdraw user data" }
    }

    suspend fun getDeletedUserStatusId(): Long {
        return withContext(Dispatchers.IO) {
            userStatusTypeRepository.findAllByIsActive(true)
        }.first { type -> type.statusTypeInfo == UserStatusTypeInfo.DELETED }.id
    }

    suspend fun getDeletedUids(deletedUserStatusId: Long, targetDate: LocalDateTime): List<Long> {
        return withContext(Dispatchers.IO) {
            userStatusHistoryRepository.getUidByToStatusIdAfter(deletedUserStatusId, targetDate)
        }
    }

    suspend fun deleteEnvelopes(uid: List<Long>) {
        val envelopes = withContext(Dispatchers.IO) {
            envelopeRepository.findAllByUidIn(uid)
        }.takeIf { envelopes -> envelopes.isNotEmpty() } ?: return

        coroutineScope {
            envelopes.chunked(DELETE_CHUNK)
                .map { chunk -> async(Dispatchers.IO) { envelopeRepository.deleteAllInBatch(chunk) } }
        }
    }

    suspend fun deleteLedgers(uid: List<Long>) {
        val ledgers = withContext(Dispatchers.IO) {
            ledgerRepository.findAllByUidIn(uid)
        }.takeIf { ledgers -> ledgers.isNotEmpty() } ?: return

        coroutineScope {
            ledgers.chunked(DELETE_CHUNK)
                .map { chunk -> async(Dispatchers.IO) { ledgerRepository.deleteAllInBatch(chunk) } }
        }
    }

    suspend fun deleteFriends(uid: List<Long>) {
        val friends = withContext(Dispatchers.IO) {
            friendRepository.findAllByUidIn(uid)
        }.takeIf { friends -> friends.isNotEmpty() } ?: return

        val friendIds = friends.map { friend -> friend.id }

        withContext(Dispatchers.IO) {
            friendRelationshipRepository.findAllByFriendIdIn(friendIds)
        }.takeIf { relationships -> relationships.isNotEmpty() }
            ?.let { relationships ->
                coroutineScope {
                    relationships.chunked(DELETE_CHUNK)
                        .map { chunk -> async(Dispatchers.IO) { friendRelationshipRepository.deleteAllInBatch(chunk) } }
                }
            }

        coroutineScope {
            friends.chunked(DELETE_CHUNK)
                .map { chunk -> async(Dispatchers.IO) { friendRepository.deleteAllInBatch(chunk) } }
        }
    }

    suspend fun deletePosts(uid: List<Long>) {
        val posts = withContext(Dispatchers.IO) {
            postRepository.findAllByUidIn(uid)
        }.takeIf { posts -> posts.isNotEmpty() } ?: return

        coroutineScope {
            posts.chunked(DELETE_CHUNK)
                .map { chunk -> async(Dispatchers.IO) { postRepository.deleteAllInBatch(chunk) } }
        }
    }
}
