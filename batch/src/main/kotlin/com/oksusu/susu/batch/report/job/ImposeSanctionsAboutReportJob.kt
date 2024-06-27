package com.oksusu.susu.batch.report.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
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

    suspend fun imposeSanctionsAboutReport() {
        logger.info { "start impose sanction about report" }

        /** 제재 완료 유저 석방 */
        freePunishedUsers()

        /** 제재 대상 식별 */
        val (punishUids, punishPostIds) = getPunishTargetIds()

        val reportResults = mutableListOf<ReportResult>()
        /** 게시물 제재 */
        punishPostIds.forEach { id ->
            reportResults.plus(
                ReportResult(
                    uid = id,
                    status = ReportResultStatus.DELETED
                )
            )
        }

        /** 유저 제재 */
        val histories = mutableListOf<UserStatusHistory>()
        val restrict7DaysUserStatusId =
            withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) }
                .first { status -> status.statusTypeInfo == UserStatusTypeInfo.ACTIVE }.id
        val statuses = withContext(Dispatchers.IO) { userStatusRepository.findAllByUid(punishUids) }
        val updatedStatuses = statuses.map { status ->
            histories.plus(
                UserStatusHistory(
                    uid = status.uid,
                    statusAssignmentType = UserStatusAssignmentType.ACCOUNT,
                    fromStatusId = status.accountStatusId,
                    toStatusId = restrict7DaysUserStatusId
                )
            )

            status.apply {
                this.accountStatusId = restrict7DaysUserStatusId
            }
        }


        coroutineScope {
            val updatePostDeferred = async { postRepository.updateIsActiveById(punishPostIds) }
            val reportResultDeferred = async { reportResultRepository.saveAll(reportResults) }
            val statusHistoryDeferred = async { userStatusHistoryRepository.saveAll(histories) }
            val statusDeferred = async { userStatusRepository.saveAll(updatedStatuses) }

            awaitAll(updatePostDeferred, reportResultDeferred, statusDeferred, statusHistoryDeferred)
        }

        logger.info { "finish impose sanction about report" }
    }

    suspend fun freePunishedUsers() {
        /** RESTRICTED_7_DAYS 대응 */
        val from = LocalDateTime.now().minusDays(7).minusHours(1)
        val to = LocalDateTime.now().minusDays(7).plusHours(1)
        val freeUid = withContext(Dispatchers.IO) { reportResultRepository.findAllByCreatedAtBetween(from, to) }
            .filter { report -> report.status == ReportResultStatus.RESTRICTED_7_DAYS }
            .map { result -> result.uid }

        /** RESTRICTED_7_DAYS 제재 해제 */
        val userStatus = withContext(Dispatchers.IO) { userStatusTypeRepository.findAllByIsActive(true) }
        val statuses = withContext(Dispatchers.IO) { userStatusRepository.findAllByUid(freeUid) }

        val activeUserStatusId = userStatus.first { status -> status.statusTypeInfo == UserStatusTypeInfo.ACTIVE }.id

        val histories = mutableListOf<UserStatusHistory>()
        val updatedStatuses = statuses.map { status ->
            histories.plus(
                UserStatusHistory(
                    uid = status.uid,
                    statusAssignmentType = UserStatusAssignmentType.ACCOUNT,
                    fromStatusId = status.accountStatusId,
                    toStatusId = activeUserStatusId
                )
            )

            status.apply {
                this.accountStatusId = activeUserStatusId
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
                .groupBy { it.uid }
                .mapValues { it.value.size.toLong() }
                .plus(userReportHistory ?: emptyMap())
                .toMutableMap()
            val postReports = reports.filter { report -> report.targetType == ReportTargetType.POST }
                .groupBy { it.uid }
                .mapValues { it.value.size.toLong() }
                .plus(postReportHistory ?: emptyMap())

            /** 5회 이상인 유저 게시물을 찾는다. */
            val punishUids = userReports.filter { it.value / 5 >= 0 }.map { report -> report.key }
            val punishPostIds = userReports.filter { it.value / 5 >= 0 }.map { report -> report.key }

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
