package com.oksusu.susu.batch.envelope.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.batch.BatchIntegrationSpec
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.cache.statistic.infrastructure.SusuEnvelopeStatisticRepository
import com.oksusu.susu.cache.statistic.infrastructure.SusuSpecificEnvelopeStatisticRepository
import com.oksusu.susu.common.config.SusuConfig
import com.oksusu.susu.common.extension.classifyKeyByPrefix
import com.oksusu.susu.common.extension.toStatisticAgeGroup
import com.oksusu.susu.common.extension.yearMonth
import com.oksusu.susu.domain.category.domain.Category
import com.oksusu.susu.domain.category.domain.CategoryAssignment
import com.oksusu.susu.domain.category.domain.vo.CategoryAssignmentType
import com.oksusu.susu.domain.category.infrastructure.CategoryAssignmentRepository
import com.oksusu.susu.domain.category.infrastructure.CategoryRepository
import com.oksusu.susu.domain.envelope.domain.Envelope
import com.oksusu.susu.domain.envelope.domain.Ledger
import com.oksusu.susu.domain.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.domain.envelope.infrastructure.EnvelopeRepository
import com.oksusu.susu.domain.envelope.infrastructure.LedgerRepository
import com.oksusu.susu.domain.friend.domain.Friend
import com.oksusu.susu.domain.friend.domain.FriendRelationship
import com.oksusu.susu.domain.friend.domain.Relationship
import com.oksusu.susu.domain.friend.infrastructure.FriendRelationshipRepository
import com.oksusu.susu.domain.friend.infrastructure.FriendRepository
import com.oksusu.susu.domain.friend.infrastructure.RelationshipRepository
import com.oksusu.susu.domain.user.domain.User
import com.oksusu.susu.domain.user.domain.vo.OAuthProvider
import com.oksusu.susu.domain.user.domain.vo.OauthInfo
import com.oksusu.susu.domain.user.infrastructure.UserRepository
import fixture.DomainFixtureUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.UUID
import kotlin.math.roundToLong

class RefreshSusuEnvelopeStatisticJobTest(
    private val refreshSusuEnvelopeStatisticJob: RefreshSusuEnvelopeStatisticJob,
    private val envelopeRepository: EnvelopeRepository,
    private val ledgerRepository: LedgerRepository,
    private val relationshipRepository: RelationshipRepository,
    private val friendRepository: FriendRepository,
    private val friendRelationshipRepository: FriendRelationshipRepository,
    private val statisticConfig: SusuConfig.StatisticConfig,
    private val adminUserConfig: SusuConfig.AdminUserConfig,
    private val categoryRepository: CategoryRepository,
    private val cacheService: CacheService,
    private val userRepository: UserRepository,
    private val categoryAssignmentRepository: CategoryAssignmentRepository,
    private val susuEnvelopeStatisticRepository: SusuEnvelopeStatisticRepository,
    private val susuSpecificEnvelopeStatisticRepository: SusuSpecificEnvelopeStatisticRepository,
) : BatchIntegrationSpec({
    val logger = KotlinLogging.logger { }
    var friends = emptyList<Friend>()
    var ledgeres = emptyList<Ledger>()
    var friendRelationships = emptyList<FriendRelationship>()
    var envelopes = emptyList<Envelope>()
    var categoryAssignments = emptyList<CategoryAssignment>()
    var categories = emptyList<Category>()
    var users = emptyList<User>()
    var relationships = emptyList<Relationship>()

    /**
     * 이 부분 private const라서 복사해옴 바꿔야함
     * TODO: private 부분 복사한거라 바꿔야함
     */
    val REFRESH_BEFORE_HOURS = 1L

    /** 월별 사용 총 금액 캐시 키 (1년) */
    val MONTHLY_SPENT_ENVELOPE_AMOUNT_FOR_LAST_YEAR_PREFIX =
        "monthly-spent-envelop-amount-for-last-year:"

    /** 관계별 총합 캐시 키 */
    val RELATIONSHIP_COUNT_PREFIX = "relationship_count:"

    /** 경조사별 총 횟수 캐싱 */
    val CATEGORY_COUNT_PREFIX = "category_count:"

    /** 나이, 카테고리, 관계별 금액 총 합 및 개수*/
    val SUSU_SPECIFIC_ENVELOPE_STATISTIC_AMOUNT_PREFIX = "susu_specific_envelope_statistic_amount:"
    val SUSU_SPECIFIC_ENVELOPE_STATISTIC_COUNT_PREFIX = "susu_specific_envelope_statistic_count:"


    beforeEach {
        /** user */
        val mockUser = DomainFixtureUtil.getUsers(10).map { user ->
            user.apply {
                this.oauthInfo = OauthInfo(
                    oAuthId = UUID.randomUUID().toString(),
                    oAuthProvider = OAuthProvider.KAKAO
                )
            }

        }
        users = withContext(Dispatchers.IO) { userRepository.saveAll(mockUser) }

        val maxUid = users.maxBy { it.id }.id
        val minUid = maxUid - users.size + 1

        /** friend */
        val mockFriends = mutableListOf<Friend>()
        for (i in 1..30) {
            mockFriends.add(
                Friend(
                    uid = (minUid..maxUid).random(),
                    name = (minUid..maxUid).random().toString()
                )
            )
        }

        /** ledger */
        val mockLedger = mutableListOf<Ledger>()
        for (i in 1..10) {
            mockLedger.add(
                Ledger(
                    uid = (minUid..maxUid).random(),
                    title = (minUid..maxUid).random().toString(),
                    startAt = LocalDateTime.now().minusDays(3),
                    endAt = LocalDateTime.now()
                )
            )
        }

        parZip(
            { withContext(Dispatchers.IO) { friendRepository.saveAll(mockFriends) } },
            { withContext(Dispatchers.IO) { ledgerRepository.saveAll(mockLedger) } },
            { withContext(Dispatchers.IO) { categoryRepository.findAllByIsActive(true) } },
            { withContext(Dispatchers.IO) { relationshipRepository.findAllByIsActive(true) } },
        ) { newFriends, newLedgeres, newCategories, newRelationships ->
            friends = newFriends
            ledgeres = newLedgeres
            categories = newCategories
            relationships = newRelationships
            val maxFriendId = newFriends.maxBy { it.id }.id
            val minFriendId = maxFriendId - newFriends.size + 1
            val maxLedgerId = newLedgeres.maxBy { it.id }.id
            val minLedgerId = maxLedgerId - newLedgeres.size + 1
            val maxRelationshipId = newRelationships.maxBy { it.id }.id
            val minRelationshipId = maxRelationshipId - newRelationships.size + 1

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


            parZip(
                { withContext(Dispatchers.IO) { friendRelationshipRepository.saveAll(mockFriendRelationships) } },
                { withContext(Dispatchers.IO) { envelopeRepository.saveAll(mockEnvelopes) } },
            ) { newFriendRelathionships, newEnvelopes ->
                friendRelationships = newFriendRelathionships
                envelopes = newEnvelopes

                val maxEnvelopeId = newEnvelopes.maxBy { it.id }.id
                val minEnvelopeId = maxEnvelopeId - newEnvelopes.size + 1
                val maxCategoryId = categories.maxBy { it.id }.id
                val minCategoryId = maxCategoryId - categories.size + 1

                /** category assignment */
                val mockCategoryAssignment = mutableListOf<CategoryAssignment>()
                for (i in minEnvelopeId..maxEnvelopeId) {
                    mockCategoryAssignment.add(
                        CategoryAssignment(
                            targetId = i,
                            targetType = CategoryAssignmentType.ENVELOPE,
                            categoryId = (minCategoryId..maxCategoryId).random()
                        )
                    )
                }
                for (i in minLedgerId..maxLedgerId) {
                    mockCategoryAssignment.add(
                        CategoryAssignment(
                            targetId = i,
                            targetType = CategoryAssignmentType.LEDGER,
                            categoryId = (minCategoryId..maxCategoryId).random()
                        )
                    )
                }

                categoryAssignments = withContext(Dispatchers.IO) {
                    categoryAssignmentRepository.saveAll(mockCategoryAssignment)
                }
            }
        }
    }

    afterEach {
        friendRelationshipRepository.deleteAllInBatch()
        friendRepository.deleteAllInBatch()
        envelopeRepository.deleteAllInBatch()
        ledgerRepository.deleteAllInBatch()
        categoryAssignmentRepository.deleteAllInBatch()
    }

    context("refresh susu envelope statistic amount") {
        it("작동시 캐시 값이 정상적으로 업데이트 되어야 한다.") {
            refreshSusuEnvelopeStatisticJob.refreshSusuEnvelopeStatisticAmount()

            delay(500)

            /** 캐시 값 분류 */
            val cachedAmount = withContext(Dispatchers.IO) {
                cacheService.getOrNull(Cache.getSusuEnvelopeStatisticAmountCache())
            }

            val monthlySpentCache =
                cachedAmount!!.classifyKeyByPrefix(MONTHLY_SPENT_ENVELOPE_AMOUNT_FOR_LAST_YEAR_PREFIX)
            val relationshipCache = cachedAmount.classifyKeyByPrefix(RELATIONSHIP_COUNT_PREFIX)
            val categoryCache = cachedAmount.classifyKeyByPrefix(CATEGORY_COUNT_PREFIX)
            val specificAmountCache = cachedAmount.classifyKeyByPrefix(SUSU_SPECIFIC_ENVELOPE_STATISTIC_AMOUNT_PREFIX)
            val specificCountCache = cachedAmount.classifyKeyByPrefix(SUSU_SPECIFIC_ENVELOPE_STATISTIC_COUNT_PREFIX)

            /** 월별 사용 총 금액 캐싱 */
            val susuEnvelopeConfig = statisticConfig.susuEnvelopeConfig

            val minEnvelopeIdx = (envelopes.size * susuEnvelopeConfig.minCuttingAverage).roundToLong()
            val maxEnvelopeIdx = (envelopes.size * susuEnvelopeConfig.maxCuttingAverage).roundToLong()

            val minEnvelopeAmount = envelopes.sortedBy { it.amount }[minEnvelopeIdx.toInt()].amount
            val maxEnvelopeAmount = envelopes.sortedBy { it.amount }[maxEnvelopeIdx.toInt()].amount

            val from = LocalDateTime.now().minusMonths(12)
            val to = LocalDateTime.now()

            envelopes.filter { it.type == EnvelopeType.SENT }
                .filter { from <= it.createdAt && it.createdAt <= to }
                .filter { it.amount in minEnvelopeAmount..maxEnvelopeAmount }
                .filter { it.uid !in adminUserConfig.adminUserUid }
                .groupBy { it.handedOverAt.yearMonth() }
                .mapValues { map -> map.value.sumOf { it.amount } }
                .forEach { (key, value) ->
                    monthlySpentCache.getOrDefault(key, null) shouldNotBe null
                    monthlySpentCache[key]?.shouldBeEqual(value)
                }

            /** 관계별 총 횟수 캐싱 */
            friendRelationships.groupBy { it.relationshipId.toString() }
                .mapValues { it.value.map { friendRelationship -> friendRelationship.friendId } }
                .forEach { (key, value) ->
                    val cacheValue = envelopes.filter { it.uid !in adminUserConfig.adminUserUid }
                        .count { it.friendId in value }

                    relationshipCache.getOrDefault(key, null) shouldNotBe null
                    relationshipCache[key]?.shouldBeEqual(cacheValue.toLong())
                }

            /** 경조사별 총 횟수 캐싱 */
            val envelopeCategoryMap = categoryAssignments.filter { it.targetType == CategoryAssignmentType.ENVELOPE }
                .groupBy { it.categoryId }
                .mapValues {
                    it.value.map { assignment -> assignment.targetId }.run {
                        envelopes.filter { envelope -> envelope.id in this }
                            .filter { envelope -> envelope.uid !in adminUserConfig.adminUserUid }
                            .count { envelope -> envelope.ledgerId == null }
                    }
                }

            val ledgerCategoryMap = categoryAssignments.filter { it.targetType == CategoryAssignmentType.LEDGER }
                .groupBy { it.categoryId }
                .mapValues {
                    it.value.map { assignment -> assignment.targetId }.run {
                        ledgeres.filter { ledger -> ledger.id in this }
                            .count { ledger -> ledger.uid !in adminUserConfig.adminUserUid }
                    }
                }

            categories.forEach { category ->
                val envelopeCategoryCount = envelopeCategoryMap.getOrDefault(category.id, 0).toLong()
                val ledgerCategoryCount = ledgerCategoryMap.getOrDefault(category.id, 0).toLong()

                categoryCache.getOrDefault(category.id.toString(), null) shouldNotBe null
                categoryCache[category.id.toString()]?.shouldBeEqual(envelopeCategoryCount + ledgerCategoryCount)
            }

            val totalAmountModels = withContext(Dispatchers.IO) {
                envelopeRepository.getCuttingTotalAmountPerStatisticGroupExceptUid(
                    minEnvelopeAmount,
                    maxEnvelopeAmount,
                    adminUserConfig.adminUserUid
                )
            }

            /** key: age, value: list<model> */
            val ages = totalAmountModels.groupBy { it.birth.toStatisticAgeGroup() }

            /** key: age:categoryId, value: list<model> */
            val ageCategorys = ages.flatMap { age ->
                val ageCategories = age.value.groupBy { it.categoryId }

                ageCategories.map { ageCategory ->
                    "${age.key}:${ageCategory.key}" to ageCategory.value
                }
            }.associate { ageCategory -> ageCategory.first to ageCategory.second }

            /** key: age:categoryId:relationshipId, value: list<model> */
            val groups = ageCategorys.flatMap { ageCategory ->
                val groups = ageCategory.value.groupBy { it.relationshipId }

                groups.map { group ->
                    "${ageCategory.key}:${group.key}" to group.value
                }
            }.associate { group -> group.first to group.second }

            /** key: age:categoryId:relationshipId, value: avg */
            groups.map { group ->
                val totalAmounts = group.value.sumOf { value -> value.totalAmounts }
                val totalCounts = group.value.sumOf { value -> value.counts }
                group.key to (totalAmounts to totalCounts)
            }.toMap().map { model ->
                specificAmountCache.getOrDefault(model.key, null) shouldNotBe null
                specificAmountCache[model.key]?.shouldBeEqual(model.value.first)

                specificCountCache.getOrDefault(model.key, null) shouldNotBe null
                specificCountCache[model.key]?.shouldBeEqual(model.value.second)
            }
        }
    }
})
