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
    private val userStatusTypeRepository: UserStatusTypeRepository,
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
    private val envelopeRepository: EnvelopeRepository,
    private val ledgerRepository: LedgerRepository,
    private val friendRepository: FriendRepository,
    private val friendRelationshipRepository: FriendRelationshipRepository,
    private val postRepository: PostRepository,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val DELETE_BEFORE_DAYS = 1L
        private const val DELETE_CHUNK = 1000
    }

    /** 기간 지정 없이 생성부터 지금까지 모든 데이터에 대해 삭제합니다. */
    suspend fun deleteWithdrawUserData() {
        logger.info { "start delete withdraw user data" }

        val targetUserStatusId = getTargetUserStatusId()

        /** 삭제된 uid */
        val targetUids = getDeletedUids(targetUserStatusId)

        logger.info { "${targetUids.toSortedSet()}탈퇴유저 정보 삭제" }

        deleteData(targetUids)

        logger.info { "finish delete withdraw user data" }
    }

    suspend fun deleteWithdrawUserDataForDay() {
        logger.info { "start delete withdraw user data for week" }

        val targetUserStatusId = getTargetUserStatusId()
        val targetDate = LocalDateTime.now().minusDays(DELETE_BEFORE_DAYS)

        /** 삭제 대상 uid */
        val targetUids = getDeletedUidsAfter(targetUserStatusId, targetDate)

        logger.info { "${targetUids.toSortedSet()}탈퇴유저 정보 삭제" }

        deleteData(targetUids)

        logger.info { "finish delete withdraw user data for week" }
    }

    private suspend fun deleteData(targetUids: List<Long>) {
        coroutineScope {
            /** 삭제 유저의 봉투 삭제 */
            val deleteEnvelopesDeferred = async { deleteEnvelopes(targetUids) }

            /** 삭제 유저의 장부 삭제 */
            val deleteLedgersDeferred = async { deleteLedgers(targetUids) }

            /** 삭제 유저의 친구 및 친구 관계 삭제 */
            val deleteFriendsDeferred = async { deleteFriends(targetUids) }

            /** 삭제 유저의 게시물 삭제 */
            val deletePostsDeferred = async { deletePosts(targetUids) }

            awaitAll(deleteEnvelopesDeferred, deleteLedgersDeferred, deleteFriendsDeferred, deletePostsDeferred)
        }
    }

    private suspend fun getTargetUserStatusId(): Long {
        return withContext(Dispatchers.IO) {
            userStatusTypeRepository.findAllByIsActive(true)
        }.first { type -> type.statusTypeInfo == UserStatusTypeInfo.DELETED }.id
    }

    private suspend fun getDeletedUidsAfter(targetUserStatusId: Long, targetDate: LocalDateTime): List<Long> {
        return withContext(Dispatchers.IO) {
            userStatusHistoryRepository.getUidByToStatusIdAfter(targetUserStatusId, targetDate)
        }
    }

    private suspend fun getDeletedUids(targetUserStatusId: Long): List<Long> {
        return withContext(Dispatchers.IO) {
            userStatusHistoryRepository.getUidByToStatusId(targetUserStatusId)
        }
    }

    private suspend fun deleteEnvelopes(uid: List<Long>) {
        val envelopes = withContext(Dispatchers.IO) {
            envelopeRepository.findAllByUidIn(uid)
        }.takeIf { envelopes -> envelopes.isNotEmpty() } ?: return

        coroutineScope {
            envelopes.chunked(DELETE_CHUNK)
                .map { chunk -> async(Dispatchers.IO) { envelopeRepository.deleteAllInBatch(chunk) } }
        }
    }

    private suspend fun deleteLedgers(uid: List<Long>) {
        val ledgers = withContext(Dispatchers.IO) {
            ledgerRepository.findAllByUidIn(uid)
        }.takeIf { ledgers -> ledgers.isNotEmpty() } ?: return

        coroutineScope {
            ledgers.chunked(DELETE_CHUNK)
                .map { chunk -> async(Dispatchers.IO) { ledgerRepository.deleteAllInBatch(chunk) } }
        }
    }

    private suspend fun deleteFriends(uid: List<Long>) {
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

    private suspend fun deletePosts(uid: List<Long>) {
        val posts = withContext(Dispatchers.IO) {
            postRepository.findAllByUidIn(uid)
        }.takeIf { posts -> posts.isNotEmpty() } ?: return

        coroutineScope {
            posts.chunked(DELETE_CHUNK)
                .map { chunk -> async(Dispatchers.IO) { postRepository.deleteAllInBatch(chunk) } }
        }
    }
}
