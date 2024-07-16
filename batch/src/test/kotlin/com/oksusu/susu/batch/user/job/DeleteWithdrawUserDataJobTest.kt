package com.oksusu.susu.batch.user.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.batch.BatchIntegrationSpec
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.envelope.domain.Ledger
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.FriendRelationship
import com.oksusu.susu.domain.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.FriendRepository
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.UserStatusType
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import com.oksusu.susu.domain.user.infrastructure.UserStatusHistoryRepository
import com.oksusu.susu.domain.user.infrastructure.UserStatusTypeRepository
import fixture.DomainFixtureUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.collections.shouldNotBeIn
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class DeleteWithdrawUserDataJobTest(
    private val deleteWithdrawUserDataJob: DeleteWithdrawUserDataJob,
    private val userStatusTypeRepository: UserStatusTypeRepository,
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
    private val envelopeRepository: EnvelopeRepository,
    private val ledgerRepository: LedgerRepository,
    private val friendRepository: FriendRepository,
    private val friendRelationshipRepository: FriendRelationshipRepository,
    private val postRepository: PostRepository,
) : BatchIntegrationSpec({
    val logger = KotlinLogging.logger { }
    val targetCount = 10L
    val minUid = 1
    val maxUid = targetCount
    var friends = emptyList<Friend>()
    var ledgeres = emptyList<Ledger>()
    var userStatusHistories = emptyList<UserStatusHistory>()
    var friendRelationships = emptyList<FriendRelationship>()
    var envelopes = emptyList<Envelope>()
    var userStatusTypes = emptyList<UserStatusType>()

    beforeEach {
        /** friend */
        val mockFriends = DomainFixtureUtil.getFriendBuilder()
            .setPostCondition { it.uid in minUid..maxUid }
            .sampleList(30)

        /** ledger */
        val mockLedger = DomainFixtureUtil.getLedgerBuilder()
            .setPostCondition { it.uid in minUid..maxUid }
            .sampleList(10)

        parZip(
            { withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) } },
            { withContext(Dispatchers.IO) { friendRepository.saveAll(mockFriends) } },
            { withContext(Dispatchers.IO) { ledgerRepository.saveAll(mockLedger) } }
        ) { newUserStatusTypes, newFriends, newLedgeres ->
            userStatusTypes = newUserStatusTypes
            friends = newFriends
            ledgeres = newLedgeres

            val deletedUserStatusTypeId = userStatusTypes
                .first { type -> type.statusTypeInfo == UserStatusTypeInfo.DELETED }.id

            /** user status history */
            val pastUserStatusHistory = mutableListOf<UserStatusHistory>()
            for (i in 1..10) {
                pastUserStatusHistory.add(
                    UserStatusHistory(
                        uid = (minUid..maxUid).random(),
                        statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                        isForced = true,
                        toStatusId = deletedUserStatusTypeId,
                        fromStatusId = (1L..4L).random()
                    )
                )
            }

            userStatusHistories =
                withContext(Dispatchers.IO) { userStatusHistoryRepository.saveAll(pastUserStatusHistory) }

            /** 과거 user status history 의 createdat 갱신 */
            withContext(Dispatchers.IO) {
                userStatusHistoryRepository.updateAllCreatedAt(
                    LocalDateTime.now().minusDays(7)
                )
            }

            val maxFriendId = newFriends.maxBy { it.id }.id
            val minFriendId = maxFriendId - newFriends.size + 1
            val maxLedgerId = newLedgeres.maxBy { it.id }.id
            val minLedgerId = maxLedgerId - newLedgeres.size + 1

            /** friend relationship */
            val mockFriendRelationships = mutableListOf<FriendRelationship>()
            for (i in 1..newFriends.size) {
                mockFriendRelationships.add(
                    FriendRelationship(
                        friendId = minFriendId + i - 1,
                        relationshipId = (1L..5L).random()
                    )
                )
            }

            /** envelope */
            val mockEnvelopes = mutableListOf<Envelope>()
            for (i in 1..25) {
                mockEnvelopes.add(
                    Envelope(
                        uid = (minUid..maxUid).random(),
                        friendId = (minFriendId..maxFriendId).random(),
                        type = EnvelopeType.SENT,
                        amount = (1L..5000000L).random(),
                        handedOverAt = LocalDateTime.now(),
                        ledgerId = (minLedgerId..maxLedgerId).random()
                    )
                )
                mockEnvelopes.add(
                    Envelope(
                        uid = (minUid..maxUid).random(),
                        friendId = (minFriendId..maxFriendId).random(),
                        type = EnvelopeType.RECEIVED,
                        amount = (1L..5000000L).random(),
                        handedOverAt = LocalDateTime.now(),
                        ledgerId = (minLedgerId..maxLedgerId).random()
                    )
                )
                mockEnvelopes.add(
                    Envelope(
                        uid = (minUid..maxUid).random(),
                        friendId = (minFriendId..maxFriendId).random(),
                        type = EnvelopeType.SENT,
                        amount = (1L..5000000L).random(),
                        handedOverAt = LocalDateTime.now(),
                        ledgerId = null
                    )
                )
                mockEnvelopes.add(
                    Envelope(
                        uid = (minUid..maxUid).random(),
                        friendId = (minFriendId..maxFriendId).random(),
                        type = EnvelopeType.RECEIVED,
                        amount = (1L..5000000L).random(),
                        handedOverAt = LocalDateTime.now(),
                        ledgerId = null
                    )
                )
            }

            /** 최신 user status history 의 createdat 갱신 */
            val latestUsetStatusHistory = mutableListOf<UserStatusHistory>()
            for (i in 1..10) {
                latestUsetStatusHistory.add(
                    UserStatusHistory(
                        uid = (minUid..maxUid).random(),
                        statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                        isForced = true,
                        toStatusId = deletedUserStatusTypeId,
                        fromStatusId = (1L..4L).random()
                    )
                )
            }

            parZip(
                { withContext(Dispatchers.IO) { friendRelationshipRepository.saveAll(mockFriendRelationships) } },
                { withContext(Dispatchers.IO) { envelopeRepository.saveAll(mockEnvelopes) } },
                { withContext(Dispatchers.IO) { userStatusHistoryRepository.saveAll(latestUsetStatusHistory) } }
            ) { newFriendRelathionships, newEnvelopes, newUserStatusHistories ->
                friendRelationships = newFriendRelathionships
                envelopes = newEnvelopes
                userStatusHistories = userStatusHistories.plus(newUserStatusHistories)
            }
        }
    }

    afterEach {
        postRepository.deleteAllInBatch()
        friendRelationshipRepository.deleteAllInBatch()
        userStatusHistoryRepository.deleteAllInBatch()
        friendRepository.deleteAllInBatch()
        envelopeRepository.deleteAllInBatch()
        ledgerRepository.deleteAllInBatch()
    }

    context("delete withdraw user data") {
        it("실행시 모든 탈퇴 유저의 기록이 삭제된다") {
            deleteWithdrawUserDataJob.deleteWithdrawUserData()

            parZip(
                { withContext(Dispatchers.IO) { envelopeRepository.findAll() } },
                { withContext(Dispatchers.IO) { ledgerRepository.findAll() } },
                { withContext(Dispatchers.IO) { friendRepository.findAll() } },
                { withContext(Dispatchers.IO) { friendRelationshipRepository.findAll() } },
                { withContext(Dispatchers.IO) { postRepository.findAll() } }
            ) {
                    envelopesAfterExecution,
                    ledgeresAfterExecution,
                    friendsAfterExecution,
                    friendRelationshipsAfterExecution,
                    postsAfterExecution,
                ->
                val deletedUserStatusTypeId = userStatusTypes
                    .first { type -> type.statusTypeInfo == UserStatusTypeInfo.DELETED }.id

                val targetUids = userStatusHistories.filter { history -> history.toStatusId == deletedUserStatusTypeId }
                    .map { it.uid }

                logger.info { targetUids.toSortedSet() }

                envelopesAfterExecution.forEach { envelope -> envelope.uid shouldNotBeIn targetUids }
                ledgeresAfterExecution.forEach { ledger -> ledger.uid shouldNotBeIn targetUids }
                friendsAfterExecution.forEach { friend -> friend.uid shouldNotBeIn targetUids }
                postsAfterExecution.forEach { post -> post.uid shouldNotBeIn targetUids }

                val targetFriendIds = friends.filter { friend -> friend.uid in targetUids }
                    .map { it.id }

                friendRelationshipsAfterExecution.forEach { relationship -> relationship.friendId shouldNotBeIn targetFriendIds }
            }
        }
    }

    context("delete withdraw user data for day") {
        it("실행시 일주일간 탈퇴 유저의 기록이 삭제된다") {
            deleteWithdrawUserDataJob.deleteWithdrawUserDataForDay()

            parZip(
                { withContext(Dispatchers.IO) { envelopeRepository.findAll() } },
                { withContext(Dispatchers.IO) { ledgerRepository.findAll() } },
                { withContext(Dispatchers.IO) { friendRepository.findAll() } },
                { withContext(Dispatchers.IO) { friendRelationshipRepository.findAll() } },
                { withContext(Dispatchers.IO) { postRepository.findAll() } },
                { withContext(Dispatchers.IO) { userStatusHistoryRepository.findAll() } }
            ) {
                    envelopesAfterExecution,
                    ledgeresAfterExecution,
                    friendsAfterExecution,
                    friendRelationshipsAfterExecution,
                    postsAfterExecution,
                    userStatusHistoriesAfterExecution,
                ->
                val deletedUserStatusTypeId = userStatusTypes
                    .first { type -> type.statusTypeInfo == UserStatusTypeInfo.DELETED }.id

                val targetUids =
                    userStatusHistoriesAfterExecution.filter { history -> history.toStatusId == deletedUserStatusTypeId }
                        .filter { history -> LocalDateTime.now().minusDays(1L) <= history.createdAt }
                        .map { it.uid }

                logger.info { targetUids.toSortedSet() }

                envelopesAfterExecution.forEach { envelope -> envelope.uid shouldNotBeIn targetUids }
                ledgeresAfterExecution.forEach { ledger -> ledger.uid shouldNotBeIn targetUids }
                friendsAfterExecution.forEach { friend -> friend.uid shouldNotBeIn targetUids }
                postsAfterExecution.forEach { post -> post.uid shouldNotBeIn targetUids }

                val targetFriendIds = friends.filter { friend -> friend.uid in targetUids }
                    .map { it.id }

                friendRelationshipsAfterExecution.forEach { relationship -> relationship.friendId shouldNotBeIn targetFriendIds }
            }
        }
    }
})
