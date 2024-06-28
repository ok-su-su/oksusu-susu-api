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
            val cacheUserReportCountDeferred = async { cacheService.set(Cache.getUserReportCountCache(), userReports) }
            val cachePostReportCountDeferred = async { cacheService.set(Cache.getPostReportCountCache(), postReports) }

            awaitAll(cacheUserReportCountDeferred, cachePostReportCountDeferred)
        }
    }

    suspend fun imposeSanctionsAboutReportForDay() {
        logger.info { "start impose sanction about report" }

        /** 제재 완료 유저 석방 */
        freePunishedUsers()

        /** 제재 대상 식별 */
        val (punishUids, punishPostIds) = getPunishTargetIds()

        /** 제재 */
        punish(punishUids, punishPostIds)

        logger.info { "finish impose sanction about report" }
    }

    suspend fun punish(punishUids: List<Long>, punishPostIds: List<Long>) {
        val reportResults = mutableListOf<ReportResult>()
        val histories = mutableListOf<UserStatusHistory>()

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

        /** 유저 제재 */
        val updatedStatuses = parZip(
            { withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) } },
            { withContext(Dispatchers.IO) { userStatusRepository.findAllByUidIn(punishUids) } }
        ) { userStatuses, statuses ->
            val restrict7DaysUserStatusId =
                userStatuses.first { status -> status.statusTypeInfo == UserStatusTypeInfo.RESTRICTED_7_DAYS }.id

            statuses.map { status ->
                histories.add(
                    UserStatusHistory(
                        uid = status.uid,
                        statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                        fromStatusId = status.communityStatusId,
                        toStatusId = restrict7DaysUserStatusId,
                        isForced = true,
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
            }
        }

        coroutineScope {
            val updatePostDeferred = async { postRepository.updateIsActiveById(punishPostIds) }
            val reportResultDeferred = async { reportResultRepository.saveAll(reportResults) }
            val statusHistoryDeferred = async { userStatusHistoryRepository.saveAll(histories) }
            val statusDeferred = async { userStatusRepository.saveAll(updatedStatuses) }

            awaitAll(updatePostDeferred, reportResultDeferred, statusDeferred, statusHistoryDeferred)
        }
    }

    suspend fun freePunishedUsers() {
        /** RESTRICTED_7_DAYS 대응 */
        val from = LocalDateTime.now().minusDays(7).minusHours(1)
        val to = LocalDateTime.now().minusDays(7).plusHours(1)
        val freeUid = withContext(Dispatchers.IO) { reportResultRepository.findAllByCreatedAtBetween(from, to) }
            .filter { report -> report.status == ReportResultStatus.RESTRICTED_7_DAYS && report.targetType == ReportTargetType.USER }
            .map { result -> result.targetId }

        /** RESTRICTED_7_DAYS 제재 해제 */
        val histories = mutableListOf<UserStatusHistory>()

        val updatedStatuses = parZip(
            { withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) } },
            { withContext(Dispatchers.IO) { userStatusRepository.findAllByUidIn(freeUid) } }
        ) { userStatus, statuses ->
            val activeUserStatusId =
                userStatus.first { status -> status.statusTypeInfo == UserStatusTypeInfo.ACTIVE }.id

            statuses.map { status ->
                histories.add(
                    UserStatusHistory(
                        uid = status.uid,
                        statusAssignmentType = UserStatusAssignmentType.COMMUNITY,
                        fromStatusId = status.communityStatusId,
                        toStatusId = activeUserStatusId,
                        isForced = true,
                    )
                )

                status.apply {
                    this.communityStatusId = activeUserStatusId
                }
            }
        }

        coroutineScope {
            val statusHistoryDeferred = async { userStatusHistoryRepository.saveAll(histories) }
            val statusDeferred = async { userStatusRepository.saveAll(updatedStatuses) }

            awaitAll(statusDeferred, statusHistoryDeferred)
        }
    }

    suspend fun getPunishTargetIds(): Pair<List<Long>, List<Long>> {
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
