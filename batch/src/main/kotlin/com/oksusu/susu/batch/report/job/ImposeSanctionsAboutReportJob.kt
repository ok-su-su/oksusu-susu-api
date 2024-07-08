package com.oksusu.susu.batch.report.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.common.extension.merge
import com.oksusu.susu.domain.post.infrastructure.repository.PostRepository
import com.oksusu.susu.domain.report.domain.ReportResult
import com.oksusu.susu.domain.report.domain.vo.ReportResultStatus
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import com.oksusu.susu.domain.report.infrastructure.ReportHistoryRepository
import com.oksusu.susu.domain.report.infrastructure.ReportResultRepository
import com.oksusu.susu.domain.user.domain.UserStatusHistory
import com.oksusu.susu.domain.user.domain.UserStatusType
import com.oksusu.susu.domain.user.domain.vo.UserStatusAssignmentType
import com.oksusu.susu.domain.user.domain.vo.UserStatusTypeInfo
import com.oksusu.susu.domain.user.infrastructure.UserStatusHistoryRepository
import com.oksusu.susu.domain.user.infrastructure.UserStatusRepository
import com.oksusu.susu.domain.user.infrastructure.UserStatusTypeRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class ImposeSanctionsAboutReportJob(
    private val reportHistoryRepository: ReportHistoryRepository,
    private val reportResultRepository: ReportResultRepository,
    private val userStatusRepository: UserStatusRepository,
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
    private val userStatusTypeRepository: UserStatusTypeRepository,
    private val cacheService: CacheService,
    private val postRepository: PostRepository,
) {
    private val logger = KotlinLogging.logger {}

    companion object {
        private const val REPORT_BEFORE_DAYS = 1L
    }

    /** 서비스 시작 시점부터 지금까지의 report 카운트를 처리합니다. */
    suspend fun updateReportCount() {
        val reports = withContext(Dispatchers.IO) { reportHistoryRepository.findAll() }

        val userReports = reports.filter { report -> report.targetType == ReportTargetType.USER }
            .groupBy { it.targetId }
            .mapValues { it.value.size.toLong() }
        val postReports = reports.filter { report -> report.targetType == ReportTargetType.POST }
            .groupBy { it.targetId }
            .mapValues { it.value.size.toLong() }

        /** 기록을 캐싱한다. */
        coroutineScope {
            val cacheUserReportCountDeferred =
                async { cacheService.set(Cache.getUserReportCountCache(), userReports) }
            val cachePostReportCountDeferred =
                async { cacheService.set(Cache.getPostReportCountCache(), postReports) }

            awaitAll(cacheUserReportCountDeferred, cachePostReportCountDeferred)
        }
    }

    /** 서비스 시작 시점부터 지금까지의 유저 커뮤니티 처벌 카운트를 처리합니다. */
    suspend fun updateUserCommunityPunishCount() {
        val userStatuses = withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) }
        val restrict7DaysUserStatusId =
            userStatuses.first { status -> status.statusTypeInfo == UserStatusTypeInfo.RESTRICTED_7_DAYS }.id

        val histories = withContext(Dispatchers.IO) {
            userStatusHistoryRepository.findAllByIsForcedAndStatusAssignmentTypeAndToStatusId(
                isForced = true,
                assignmentType = UserStatusAssignmentType.COMMUNITY,
                toStatusId = restrict7DaysUserStatusId
            )
        }

        val userCommunityPunishedCount = histories.groupBy { it.uid }
            .mapValues { it.value.size.toLong() }

        cacheService.set(Cache.getUserCommunityPunishedCountCache(), userCommunityPunishedCount)
    }

    suspend fun imposeSanctionsAboutReportForDay() {
        logger.info { "start impose sanction about report" }

        val userStatuses = withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) }

        /** 제재 완료 유저 석방 */
        freePunishedUsers(userStatuses)

        /** 제재 대상 식별 */
        val (punishUids, punishPostIds) = getPunishTargetIds()

        /** 제재 */
        punish(punishUids, punishPostIds, userStatuses)

        logger.info { "finish impose sanction about report" }
    }

    private suspend fun punish(punishUids: List<Long>, punishPostIds: List<Long>, userStatuses: List<UserStatusType>) {
        val reportResults = mutableListOf<ReportResult>()
        val histories = mutableListOf<UserStatusHistory>()
        val restrict7DaysUserStatusId =
            userStatuses.first { status -> status.statusTypeInfo == UserStatusTypeInfo.RESTRICTED_7_DAYS }.id
        val banishedUserStatusId =
            userStatuses.first { status -> status.statusTypeInfo == UserStatusTypeInfo.BANISHED }.id

        /** 게시물 제재 */
        punishPostIds.forEach { id ->
            reportResults.add(
                ReportResult(
                    targetId = id,
                    targetType = ReportTargetType.POST,
                    status = ReportResultStatus.DELETED
                )
            )
        }

        /** 제재 게시물 글쓴이 조회 */
        val punishPostUids = withContext(Dispatchers.IO) { postRepository.findAllByIdIn(punishPostIds) }
            .map { post -> post.uid }

        val targetUids = punishUids.toSet().plus(punishPostUids)

        logger.info { "${targetUids.sorted()} 유저 제재 및 ${punishPostIds.sorted()} 게시글 삭제" }

        /** 유저 제재 */
        val punishedCountMap = mutableMapOf<Long, Long>()

        val updatedStatuses = parZip(
            { withContext(Dispatchers.IO) { userStatusRepository.findAllByUidIn(targetUids) } },
            { withContext(Dispatchers.IO) { cacheService.getOrNull(Cache.getUserCommunityPunishedCountCache()) } }
        ) { statuses, userCommunityPublishedCount ->
            statuses.map { status ->
                val punishedCount = userCommunityPublishedCount!!.getOrDefault(status.uid, 0)

                punishedCountMap[status.uid] = punishedCount + 1

                /**
                 * 신고 누적 횟수가 3번일 경우 커뮤니티 이용 정지이므로
                 * 누적 횟수가 2 미만이면 7일 정지
                 * 2 이상이면 커뮤니티 밴
                 */
                if (punishedCount < 2L) {
                    histories.add(
                        UserStatusHistory(
                            uid = status.uid,
                            statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                            fromStatusId = status.communityStatusId,
                            toStatusId = restrict7DaysUserStatusId,
                            isForced = true
                        )
                    )

                    reportResults.add(
                        ReportResult(
                            targetId = status.uid,
                            targetType = ReportTargetType.USER,
                            status = ReportResultStatus.RESTRICTED_7_DAYS
                        )
                    )

                    status.apply {
                        this.communityStatusId = restrict7DaysUserStatusId
                    }
                } else {
                    logger.info { "${status.uid} 유저 커뮤니티 영구 정지" }
                    histories.add(
                        UserStatusHistory(
                            uid = status.uid,
                            statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                            fromStatusId = status.communityStatusId,
                            toStatusId = banishedUserStatusId,
                            isForced = true
                        )
                    )

                    reportResults.add(
                        ReportResult(
                            targetId = status.uid,
                            targetType = ReportTargetType.USER,
                            status = ReportResultStatus.BANISHED
                        )
                    )

                    status.apply {
                        this.communityStatusId = banishedUserStatusId
                    }
                }
            }
        }

        coroutineScope {
            val updatePostDeferred = async { postRepository.updateIsActiveById(punishPostIds) }
            val reportResultDeferred = async { reportResultRepository.saveAll(reportResults) }
            val statusHistoryDeferred = async { userStatusHistoryRepository.saveAll(histories) }
            val statusDeferred = async { userStatusRepository.saveAll(updatedStatuses) }
            val punishedCountDeferred =
                async { cacheService.set(Cache.getUserCommunityPunishedCountCache(), punishedCountMap) }

            awaitAll(
                updatePostDeferred,
                reportResultDeferred,
                statusDeferred,
                statusHistoryDeferred,
                punishedCountDeferred
            )
        }
    }

    private suspend fun freePunishedUsers(userStatuses: List<UserStatusType>) {
        /** RESTRICTED_7_DAYS 대응 */
        val from = LocalDateTime.now().minusDays(7).minusHours(1)
        val to = LocalDateTime.now().minusDays(7).plusHours(1)
        val freeUid = withContext(Dispatchers.IO) { reportResultRepository.findAllByCreatedAtBetween(from, to) }
            .filter { report -> report.status == ReportResultStatus.RESTRICTED_7_DAYS && report.targetType == ReportTargetType.USER }
            .map { result -> result.targetId }
            .toSet()

        logger.info { "${freeUid.sorted()}유저 석방" }

        /** RESTRICTED_7_DAYS 제재 해제 */
        val histories = mutableListOf<UserStatusHistory>()

        val activeUserStatusId =
            userStatuses.first { status -> status.statusTypeInfo == UserStatusTypeInfo.ACTIVE }.id

        val statuses = withContext(Dispatchers.IO) { userStatusRepository.findAllByUidIn(freeUid.toSet()) }
        val updatedStatuses = statuses.map { status ->
            histories.add(
                UserStatusHistory(
                    uid = status.uid,
                    statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                    fromStatusId = status.communityStatusId,
                    toStatusId = activeUserStatusId,
                    isForced = true
                )
            )

            status.apply {
                this.communityStatusId = activeUserStatusId
            }
        }

        coroutineScope {
            val statusHistoryDeferred = async { userStatusHistoryRepository.saveAll(histories) }
            val statusDeferred = async { userStatusRepository.saveAll(updatedStatuses) }

            awaitAll(statusDeferred, statusHistoryDeferred)
        }
    }

    private suspend fun getPunishTargetIds(): Pair<List<Long>, List<Long>> {
        val targetDate = LocalDateTime.now().minusDays(REPORT_BEFORE_DAYS)

        return parZip(
            { withContext(Dispatchers.IO) { reportHistoryRepository.findAllByCreatedAtAfter(targetDate) } },
            { withContext(Dispatchers.IO) { cacheService.getOrNull(Cache.getUserReportCountCache()) } },
            { withContext(Dispatchers.IO) { cacheService.getOrNull(Cache.getPostReportCountCache()) } }
        ) { reports, userReportHistory, postReportHistory ->
            /** 일주일간 기록과 7일 전까지의 기록을 병합한다 */
            val userReports = reports.filter { report -> report.targetType == ReportTargetType.USER }
                .groupBy { it.targetId }
                .mapValues { it.value.size.toLong() }
                .merge(userReportHistory!!)
                .toMutableMap()

            val postReports = reports.filter { report -> report.targetType == ReportTargetType.POST }
                .groupBy { it.targetId }
                .mapValues { it.value.size.toLong() }
                .merge(postReportHistory!!)

            /** 5회 이상인 유저 게시물을 찾는다. */
            val punishUids = userReports.filter { it.value / 5 > 0 }.map { report -> report.key }
            val punishPostIds = postReports.filter { it.value / 5 > 0 }.map { report -> report.key }

            /** 유저 기록의 경우 초기화한다. */
            for (uid in punishUids) {
                userReports[uid] = userReports[uid]!! % 5
            }

            /** 기록을 캐싱한다. */
            val cacheUserReportCountDeferred = async { cacheService.set(Cache.getUserReportCountCache(), userReports) }
            val cachePostReportCountDeferred = async { cacheService.set(Cache.getPostReportCountCache(), postReports) }

            awaitAll(cacheUserReportCountDeferred, cachePostReportCountDeferred)

            punishUids to punishPostIds
        }
    }
}
