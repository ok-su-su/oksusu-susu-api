package com.oksusu.susu.batch.report.job

import arrow.fx.coroutines.parZip
import com.oksusu.susu.batch.BatchIntegrationSpec
import com.oksusu.susu.cache.key.Cache
import com.oksusu.susu.cache.service.CacheService
import com.oksusu.susu.domain.report.domain.vo.ReportTargetType
import com.oksusu.susu.domain.report.infrastructure.ReportHistoryRepository
import com.oksusu.susu.domain.user.infrastructure.UserStatusHistoryRepository
import fixture.DomainFixtureUtil
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class ImposeSanctionsAboutReportJobTest(
    private val imposeSanctionsAboutReportJob: ImposeSanctionsAboutReportJob,
    private val reportHistoryRepository: ReportHistoryRepository,
    private val cacheService: CacheService,
    private val userStatusHistoryRepository: UserStatusHistoryRepository,
) : BatchIntegrationSpec({
    val logger = KotlinLogging.logger { }

    describe("update report count") {
        context("실행시") {
            val historySize = 100
            val histories = DomainFixtureUtil.getReportHistorys(historySize)

            beforeEach {
                withContext(Dispatchers.IO) { reportHistoryRepository.saveAll(histories) }
            }

            afterEach {
                reportHistoryRepository.deleteAllInBatch()
            }

            it("유저별 신고 개수와 게시물별 신고 개수가 캐싱된다.") {
                imposeSanctionsAboutReportJob.updateReportCount()

                delay(1000)

                parZip(
                    { cacheService.getOrNull(Cache.getPostReportCountCache()) },
                    { cacheService.getOrNull(Cache.getUserReportCountCache()) },
                ) { cachedPostReport, cachedUserReport ->
                    /** user report count 수 검증 */
                    histories.filter { report -> report.targetType == ReportTargetType.USER }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .map { count ->
                            cachedUserReport shouldNotBe null
                            cachedUserReport?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }

                    /** post report count 수 검증 */
                    histories.filter { report -> report.targetType == ReportTargetType.POST }
                        .groupBy { it.targetId }
                        .mapValues { it.value.size.toLong() }
                        .map { count ->
                            cachedPostReport shouldNotBe null
                            cachedPostReport?.getOrDefault(count.key, -1)?.shouldBeEqual(count.value)
                        }
                }
            }
        }
    }

    describe("update user community punish count") {
        context("실행시") {
            it("유저별 커뮤니티 제재 횟수가 캐싱된다.") {
            }
        }
    }

    describe("get punish target ids") {
        context("실행시") {
            it("제재 대상을 식별할 수 있다.") {
            }
        }
    }

    describe("free punished users") {
        context("실행시") {
            it("제재를 해제해준다.") {
            }
        }
    }

    describe("punish") {
        context("실행시") {
            it("제재 대상을 처벌한다.") {
            }
        }
    }

    describe("impose sanctions about report for day") {
        context("실행시") {
            it("처벌 및 제제 해제를 한다.") {
            }
        }
    }

})
