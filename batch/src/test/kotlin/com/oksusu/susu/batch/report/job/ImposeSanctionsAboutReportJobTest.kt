package com.oksusu.susu.batch.report.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.batch.BatchIntegrationSpec
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.common.extension.merge
import com.oksusu.susu.domain.post.domain.Post
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.report.domain.ReportHistory
import com.oksusu.susu.domain.report.domain.vo.ReportResultStatus
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import com.oksusu.susu.domain.report.infrastructure.ReportHistoryRepository
import com.oksusu.susu.domain.report.infrastructure.ReportResultRepository
import com.oksusu.susu.domain.user.domain.UserStatus
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import com.oksusu.susu.domain.user.infrastructure.UserStatusHistoryRepository
import com.oksusu.susu.domain.user.infrastructure.UserStatusRepository
import com.oksusu.susu.domain.user.infrastructure.UserStatusTypeRepository
import fixture.DomainFixtureUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.*
import java.time.LocalDateTime

class ImposeSanctionsAboutReportJobTest(
    private val imposeSanctionsAboutReportJob: ImposeSanctionsAboutReportJob,
    private val reportHistoryRepository: ReportHistoryRepository,
    private val cacheService: CacheService,
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
    private val userStatusTypeRepository: UserStatusTypeRepository,
    private val postRepository: PostRepository,
    private val userStatusRepository: UserStatusRepository,
    private val reportResultRepository: ReportResultRepository,
) : BatchIntegrationSpec({
    val logger = KotlinLogging.logger { }

    describe("update report count") {
        context("실행시") {
            val historySize = 100
            val histories = DomainFixtureUtil.getReportHistories(historySize)

            beforeEach {
                withContext(Dispatchers.IO) { reportHistoryRepository.saveAll(histories) }
            }

            afterEach {
                reportHistoryRepository.deleteAllInBatch()
                cacheService.delete(Cache.getUserReportCountCache())
                cacheService.delete(Cache.getPostReportCountCache())
            }

            it("유저별 신고 개수와 게시물별 신고 개수가 캐싱된다.") {
                imposeSanctionsAboutReportJob.updateReportCount()

                delay(500)

                parZip(
                    { cacheService.getOrNull(Cache.getPostReportCountCache()) },
                    { cacheService.getOrNull(Cache.getUserReportCountCache()) }
                ) { cachedPostReport, cachedUserReport ->
                    /** user report count 수 검증 */
                    cachedUserReport shouldNotBe null

                    histories.filter { report -> report.targetType == ReportTargetType.USER }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .map { count ->
                            cachedUserReport?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }

                    /** post report count 수 검증 */
                    cachedPostReport shouldNotBe null

                    histories.filter { report -> report.targetType == ReportTargetType.POST }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .map { count ->
                            cachedPostReport?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }
                }
            }
        }
    }

    describe("update user community punish count") {
        context("실행시") {
            val historySize = 100
            val histories = DomainFixtureUtil.getUserStatusHistories(historySize)

            beforeEach {
                withContext(Dispatchers.IO) { userStatusHistoryRepository.saveAll(histories) }
            }

            afterEach {
                userStatusHistoryRepository.deleteAllInBatch()
                cacheService.delete(Cache.getUserCommunityPunishedCountCache())
            }

            it("유저별 커뮤니티 제재 횟수가 캐싱된다.") {
                imposeSanctionsAboutReportJob.updateUserCommunityPunishCount()

                delay(500)

                parZip(
                    { withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) } },
                    { cacheService.getOrNull(Cache.getUserCommunityPunishedCountCache()) }
                ) { userStatuses, cachedUserCommunityPunishCount ->
                    val restrict7DaysUserStatusId =
                        userStatuses.first { status -> status.statusTypeInfo == UserStatusTypeInfo.RESTRICTED_7_DAYS }.id

                    cachedUserCommunityPunishCount shouldNotBe null

                    histories.filter { history ->
                        history.isForced &&
                            history.statusAssignmentType == UserStatusAssignmentType.COMMUNITY &&
                            history.toStatusId == restrict7DaysUserStatusId
                    }
                        .groupBy { it.uid }
                        .mapValues { it.value.size.toLong() }
                        .map { count ->
                            cachedUserCommunityPunishCount?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }
                }
            }
        }
    }

    describe("impose sanctions about report for day") {
        context("실행시") {
            val targetCount = 10L
            val idJitter = 100L
            val minPostIdAndUserId = idJitter + 1
            val maxPostIdAndUserId = idJitter + targetCount
            val reportBeforeDay = 1L
            var reportHistorys = emptyList<ReportHistory>()
            var userStatusHistories = emptyList<UserStatusHistory>()
            var restrict7DaysUserStatusTypeId = 0L
            var banishedUserStatusTypeId = 0L
            var activeUserStatusTypeId = 0L

            beforeEach {
                /** user status */
                val mockUserStatuses = mutableListOf<UserStatus>()
                for (i in 1..targetCount) {
                    val status = DomainFixtureUtil.getUserStatusBuilder()
                        .set("uid", i + idJitter)
                        .sample()
                    mockUserStatuses.add(status)
                }

                /** user status history */
                val mockUserStatusHistories = mutableListOf<UserStatusHistory>()
                for (i in 1..10) {
                    mockUserStatusHistories.add(
                        UserStatusHistory(
                            uid = (minPostIdAndUserId..maxPostIdAndUserId).random(),
                            statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                            isForced = true,
                            toStatusId = 3L,
                            fromStatusId = (1L..4L).random()
                        )
                    )
                }

//                val mockUserStatusHistories = DomainFixtureUtil.getUserStatusHistoryBuilder()
//                    .setPostCondition { history -> history.uid in 1 + idJitter..targetCount + idJitter  }
//                    .set("isForced", true)
//                    .set("statusAssignmentType", UserStatusAssignmentType.COMMUNITY)
//                    .set("toStatusId", 3L)
//                    .sampleList(10)

                /** report result */
                val mockReportResults =
                    DomainFixtureUtil.getReportResults(20, 1 + idJitter, targetCount + idJitter)

                /** report history */
                val mockReportHistorys = mutableListOf<ReportHistory>()
                for (i in 1..50) {
                    mockReportHistorys.add(
                        ReportHistory(
                            uid = (0L..10000L).random(),
                            targetId = (minPostIdAndUserId..maxPostIdAndUserId).random(),
                            targetType = ReportTargetType.USER,
                            metadataId = 1L
                        )
                    )
                    mockReportHistorys.add(
                        ReportHistory(
                            uid = (0L..10000L).random(),
                            targetId = (minPostIdAndUserId..maxPostIdAndUserId).random(),
                            targetType = ReportTargetType.POST,
                            metadataId = 1L
                        )
                    )
                }

//                val mockReportHistorys = DomainFixtureUtil.getReportHistoryBuilder()
//                    .setPostCondition("targetId", Long::class.java) { targetId ->
//                        targetId in 101L..110L
//                    }
//                    .setPostCondition {  minPostIdAndUserId <= it.targetId }
//                    .sampleList(100)

                /** post */
                val mockPosts = mutableListOf<Post>()
                for (i in 1..targetCount) {
                    val post = DomainFixtureUtil.getPostBuilder()
                        .set("id", i + idJitter)
                        .setPostCondition("uid", Long::class.java) { uid ->
                            uid in 101L..110L
                        }
                        .sample()
                    mockPosts.add(post)
                }

                parZip(
                    { withContext(Dispatchers.IO) { postRepository.saveAll(mockPosts) } },
                    { withContext(Dispatchers.IO) { userStatusHistoryRepository.saveAll(mockUserStatusHistories) } },
                    { withContext(Dispatchers.IO) { userStatusRepository.saveAll(mockUserStatuses) } },
                    { withContext(Dispatchers.IO) { reportResultRepository.saveAll(mockReportResults) } },
                    { withContext(Dispatchers.IO) { reportHistoryRepository.saveAll(mockReportHistorys) } },
                    { withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) } },
                    { cacheService.set(Cache.getUserReportCountCache(), emptyMap()) },
                    { cacheService.set(Cache.getPostReportCountCache(), emptyMap()) },
                    { cacheService.set(Cache.getUserCommunityPunishedCountCache(), emptyMap()) }
                ) { _, savedUserStatusHistories, _, _, savedReportHistories, savedUserStatusTypes, _, _, _ ->
                    reportHistorys = savedReportHistories
                    userStatusHistories = savedUserStatusHistories

                    coroutineScope {
                        val updateUserStatusDeferred =
                            async { userStatusRepository.updateAllCreatedAt(LocalDateTime.now().minusDays(7)) }
                        val updateReportResultDeferred =
                            async { reportResultRepository.updateAllCreatedAt(LocalDateTime.now().minusDays(7)) }
                        val updateReportHistoryDeferred =
                            async { reportHistoryRepository.updateAllCreatedAt(LocalDateTime.now().minusMinutes(20)) }

                        awaitAll(updateReportHistoryDeferred, updateReportResultDeferred, updateUserStatusDeferred)
                    }

                    restrict7DaysUserStatusTypeId =
                        savedUserStatusTypes.first { status -> status.statusTypeInfo == UserStatusTypeInfo.RESTRICTED_7_DAYS }.id
                    banishedUserStatusTypeId =
                        savedUserStatusTypes.first { status -> status.statusTypeInfo == UserStatusTypeInfo.BANISHED }.id
                    activeUserStatusTypeId =
                        savedUserStatusTypes.first { status -> status.statusTypeInfo == UserStatusTypeInfo.ACTIVE }.id
                }
            }

            afterEach {
                postRepository.deleteAllInBatch()
                userStatusRepository.deleteAllInBatch()
                userStatusHistoryRepository.deleteAllInBatch()
                reportResultRepository.deleteAllInBatch()
                reportHistoryRepository.deleteAllInBatch()

                coroutineScope {
                    val cacheUserReportCountDeferred =
                        async { cacheService.set(Cache.getUserReportCountCache(), emptyMap()) }
                    val cachePostReportCountDeferred =
                        async { cacheService.set(Cache.getPostReportCountCache(), emptyMap()) }
                    val cacheUserCommunityPunishedCountDeferred = async {
                        cacheService.set(Cache.getUserCommunityPunishedCountCache(), emptyMap())
                    }

                    awaitAll(
                        cacheUserReportCountDeferred,
                        cachePostReportCountDeferred,
                        cacheUserCommunityPunishedCountDeferred
                    )
                }
            }

            it("이전 기록이 없을 때, 처벌 및 제제 해제를 한다.") {
                imposeSanctionsAboutReportJob.imposeSanctionsAboutReportForDay()

                delay(500)

                /** 제재 타겟 확인 */
                val targetDate = LocalDateTime.now().minusDays(reportBeforeDay)
                val reports = reportHistorys.filter { targetDate < it.createdAt }

                val punishUids = reports.filter { report -> report.targetType == ReportTargetType.USER }
                    .groupBy { it.targetId }
                    .mapValues { it.value.size.toLong() }
                    .filter { it.value / 5 > 0 }
                    .map { report -> report.key }
                    .toList()
                val punishPostIds = reports.filter { report -> report.targetType == ReportTargetType.POST }
                    .groupBy { it.targetId }
                    .mapValues { it.value.size.toLong() }
                    .filter { it.value / 5 > 0 }
                    .map { report -> report.key }
                    .toList()

                parZip(
                    { withContext(Dispatchers.IO) { userStatusRepository.findAll() } },
                    { withContext(Dispatchers.IO) { userStatusHistoryRepository.findAll() } },
                    { withContext(Dispatchers.IO) { reportResultRepository.findAll() } },
                    { withContext(Dispatchers.IO) { postRepository.findAll() } },
                    { cacheService.getOrNull(Cache.getUserReportCountCache()) },
                    { cacheService.getOrNull(Cache.getPostReportCountCache()) },
                    { cacheService.getOrNull(Cache.getUserCommunityPunishedCountCache()) }
                ) { newUserStatuses, newUserStatusHistories, newReportResults, newPosts, userReportHistory, postReportHistory, userCommunityPublishedCount ->
                    /** 제재 대상 유저 */
                    val punishedPostUids = newPosts.filter { post -> post.id in punishPostIds }
                        .map { post -> post.uid }

                    val punishTargetUids = punishedPostUids.toSet()
                        .plus(punishUids.toSet())

                    /** 석방 유저 확인 */
                    val from = LocalDateTime.now().minusDays(7).minusHours(1)
                    val to = LocalDateTime.now().minusDays(7).plusHours(1)
                    val freeUid = newReportResults
                        .filter { report ->
                            from <= report.createdAt && report.createdAt <= to &&
                                report.status == ReportResultStatus.RESTRICTED_7_DAYS &&
                                report.targetType == ReportTargetType.USER
                        }.map { result -> result.targetId }
                        .toSet()

                    /** 석방 확인 */
                    // userStatus는 결국 정지된 유저는 정지로 표시되므로 완전 석방만 따로 확인
                    val freeTargetUid = freeUid.minus(punishTargetUids.toSet())
                    newUserStatuses.filter { status -> status.uid in freeTargetUid }
                        .map { status -> status.communityStatusId shouldBeEqual activeUserStatusTypeId }
                    // history는 석방 -> 제재 라도 2번 남아서 석방 대상은 모두 history를 남김
                    newUserStatusHistories.filter { history ->
                        history.isForced &&
                            history.toStatusId == activeUserStatusTypeId &&
                            LocalDateTime.now().minusMinutes(10) < history.createdAt &&
                            history.statusAssignmentType == UserStatusAssignmentType.COMMUNITY
                    }.run { this.size shouldBeEqual freeUid.size }

                    /** 제재 확인 - 게시물 삭제 */
                    newPosts.filter { post -> post.id in punishPostIds }
                        .map { post ->
                            post.isActive shouldBeEqual false
                        }

                    /** 제재 확인 - 유저 커뮤니티 정지 */
                    newUserStatuses.filter { status -> status.uid in punishTargetUids }
                        .map { status -> status.communityStatusId shouldBeEqual restrict7DaysUserStatusTypeId }
                    newUserStatusHistories.filter { history ->
                        history.isForced &&
                            history.statusAssignmentType == UserStatusAssignmentType.COMMUNITY &&
                            history.toStatusId == restrict7DaysUserStatusTypeId &&
                            LocalDateTime.now().minusMinutes(10) <= history.createdAt
                    }.map { history -> history.uid in punishTargetUids }
                    newReportResults.filter { result ->
                        result.targetType == ReportTargetType.USER &&
                            result.status == ReportResultStatus.RESTRICTED_7_DAYS
                    }.map { result -> result.targetId in punishTargetUids }

                    /** 유저 신고 누적 횟수 캐시 검증 */
                    reports.filter { report -> report.targetType == ReportTargetType.USER }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() % 5 }
                        .map { count ->
                            userReportHistory?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }

                    /** 게시물 신고 누적 횟수 캐시 검증 */
                    reports.filter { report -> report.targetType == ReportTargetType.POST }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .map { count ->
                            postReportHistory?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }

                    /** 유저 커뮤니티 처벌 누적 횟수 캐시 검증 */
                    punishedPostUids.forEach { uid ->
                        userCommunityPublishedCount?.getOrDefault(uid, -1)?.shouldBeEqual(1L)
                    }
                }
            }

            it("이전 기록이 있을 때, 처벌 및 제제 해제를 한다.") {
                /** job 실행용 레디스 초기화 및 값 저장 */
                imposeSanctionsAboutReportJob.updateUserCommunityPunishCount()
                imposeSanctionsAboutReportJob.updateReportCount()

                delay(500)

                val originCachedUserReportCount = cacheService.getOrNull(Cache.getUserReportCountCache())
                val originCachedPostReportCount = cacheService.getOrNull(Cache.getPostReportCountCache())
                val originCachedUserCommunityPublishedCount =
                    cacheService.getOrNull(Cache.getUserCommunityPunishedCountCache())

                imposeSanctionsAboutReportJob.imposeSanctionsAboutReportForDay()

                delay(500)

                /** 제재 타겟 확인 */
                val targetDate = LocalDateTime.now().minusDays(reportBeforeDay)

                parZip(
                    { withContext(Dispatchers.IO) { reportHistoryRepository.findAllByCreatedAtAfter(targetDate) } },
                    { withContext(Dispatchers.IO) { userStatusRepository.findAll() } },
                    { withContext(Dispatchers.IO) { userStatusHistoryRepository.findAll() } },
                    { withContext(Dispatchers.IO) { reportResultRepository.findAll() } },
                    { withContext(Dispatchers.IO) { postRepository.findAll() } },
                    { cacheService.getOrNull(Cache.getUserReportCountCache()) },
                    { cacheService.getOrNull(Cache.getPostReportCountCache()) },
                    { cacheService.getOrNull(Cache.getUserCommunityPunishedCountCache()) }
                ) {
                        reports,
                        newUserStatuses,
                        newUserStatusHistories,
                        newReportResults,
                        newPosts,
                        newCachedUserReportCount,
                        newCachedPostReportCount,
                        newCachedUserCommunityPublishedCount,
                    ->
                    /** 일주일간 기록과 7일 전까지의 기록을 병합한다 */
                    val userReports = reportHistorys.filter { report -> report.targetType == ReportTargetType.USER }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .merge(originCachedUserReportCount!!)

                    val postReports = reportHistorys.filter { report -> report.targetType == ReportTargetType.POST }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .merge(originCachedPostReportCount!!)

                    /** 5회 이상인 유저 게시물을 찾는다. */
                    val punishUids = userReports.filter { it.value / 5 > 0 }.map { report -> report.key }
                    val punishPostIds = postReports.filter { it.value / 5 > 0 }.map { report -> report.key }

                    /** 영구 정지 제재 대상 확인 */
                    val banUids = userStatusHistories.filter { history ->
                        history.isForced &&
                            history.statusAssignmentType == UserStatusAssignmentType.COMMUNITY &&
                            history.toStatusId == restrict7DaysUserStatusTypeId
                    }.groupBy { it.uid }
                        .mapValues { it.value.size.toLong() }
                        .filter { count -> count.value >= 3L || (count.value == 2L && count.key in punishUids) }
                        .map { it.key }

                    /** 제재 대상 유저 */
                    val punishedPostUids = newPosts.filter { post -> post.id in punishPostIds }
                        .map { post -> post.uid }

                    val punishTargetUids = punishedPostUids.toSet()
                        .plus(punishUids.toSet())
                        .minus(banUids.toSet())

                    /** 석방 유저 확인 */
                    val from = LocalDateTime.now().minusDays(7).minusHours(1)
                    val to = LocalDateTime.now().minusDays(7).plusHours(1)
                    val freeUid = newReportResults.filter { report ->
                        from <= report.createdAt && report.createdAt <= to &&
                            report.status == ReportResultStatus.RESTRICTED_7_DAYS &&
                            report.targetType == ReportTargetType.USER
                    }.map { result -> result.targetId }
                        .toSet()

                    /** 석방 확인 */
                    // userStatus는 결국 정지된 유저는 정지로 표시되므로 완전 석방만 따로 확인
                    val freeTargetUid = freeUid.minus(punishTargetUids.toSet())
                        .minus(banUids.toSet())
                    newUserStatuses.filter { status -> status.uid in freeTargetUid }
                        .map { status -> status.communityStatusId shouldBeEqual activeUserStatusTypeId }
                    // history는 석방 -> 제재 라도 2번 남아서 석방 대상은 모두 history를 남김
                    newUserStatusHistories.filter { history ->
                        history.isForced &&
                            history.toStatusId == activeUserStatusTypeId &&
                            LocalDateTime.now().minusMinutes(10) < history.createdAt &&
                            history.statusAssignmentType == UserStatusAssignmentType.COMMUNITY
                    }.run { this.size shouldBeEqual freeUid.size }

                    /** 제재 확인 - 게시물 삭제 */
                    newPosts.filter { post -> post.id in punishPostIds }
                        .map { post ->
                            post.isActive shouldBeEqual false
                        }

                    /** 제재 확인 - 유저 커뮤니티 정지 */
                    newUserStatuses.filter { status -> status.uid in punishTargetUids }
                        .map { status -> status.communityStatusId shouldBeEqual restrict7DaysUserStatusTypeId }
                    newUserStatusHistories.filter { history ->
                        history.isForced &&
                            history.statusAssignmentType == UserStatusAssignmentType.COMMUNITY &&
                            history.toStatusId == restrict7DaysUserStatusTypeId &&
                            LocalDateTime.now().minusMinutes(10) <= history.createdAt
                    }.map { history -> history.uid in punishTargetUids }
                    newReportResults.filter { result ->
                        result.targetType == ReportTargetType.USER &&
                            result.status == ReportResultStatus.RESTRICTED_7_DAYS
                    }.map { result -> result.targetId in punishTargetUids }

                    /** 제재 확인 - 유저 커뮤니티 영구 밴 */
                    newUserStatuses.filter { status -> status.uid in banUids }
                        .map { status -> status.communityStatusId shouldBeEqual banishedUserStatusTypeId }
                    newUserStatusHistories.filter { history ->
                        history.isForced &&
                            history.statusAssignmentType == UserStatusAssignmentType.COMMUNITY &&
                            history.toStatusId == banishedUserStatusTypeId &&
                            LocalDateTime.now().minusMinutes(10) <= history.createdAt
                    }.run { this.size shouldBeEqual banUids.size }
                    newReportResults.filter { result ->
                        result.targetType == ReportTargetType.USER &&
                            result.status == ReportResultStatus.BANISHED
                    }.map { result -> result.targetId in banUids }

                    /** 유저 신고 누적 횟수 캐시 검증 */
                    reports.filter { report -> report.targetType == ReportTargetType.USER }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .merge(originCachedUserReportCount)
                        .map { count ->
                            newCachedUserReportCount?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value % 5)
                        }

                    /** 게시물 신고 누적 횟수 캐시 검증 */
                    reports.filter { report -> report.targetType == ReportTargetType.POST }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .merge(originCachedPostReportCount)
                        .map { count ->
                            newCachedPostReportCount?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }

                    /** 유저 커뮤니티 처벌 누적 횟수 캐시 검증 */
                    newCachedUserCommunityPublishedCount?.map { count ->
                        val uid = count.key
                        val origin = originCachedUserCommunityPublishedCount?.getOrDefault(uid, 0)

                        if (uid in punishTargetUids || uid in banUids) {
                            count.value shouldBeEqual origin!! + 1L
                        } else {
                            count.value shouldBeEqual origin!!
                        }
                    }
                }
            }
        }
    }
})
