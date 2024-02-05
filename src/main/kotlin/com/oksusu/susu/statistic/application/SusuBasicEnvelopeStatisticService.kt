package com.oksusu.susu.statistic.application

import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.envelope.infrastructure.model.CountPerHandedOverAtModel
import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToExecuteException
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.friend.infrastructure.model.CountPerRelationshipIdModel
import com.oksusu.susu.statistic.domain.SusuBasicEnvelopeStatistic
import com.oksusu.susu.statistic.infrastructure.redis.SusuBasicEnvelopeStatisticRepository
import com.oksusu.susu.statistic.model.TitleValueModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class SusuBasicEnvelopeStatisticService(
    private val susuBasicEnvelopeStatisticRepository: SusuBasicEnvelopeStatisticRepository,
    private val categoryService: CategoryService,
    private val relationshipService: RelationshipService,
) {
    val logger = KotlinLogging.logger { }

    suspend fun constructBasicStatistic(
        envelopHandOverAtMonthCount: List<CountPerHandedOverAtModel>,
        relationShipConuts: List<CountPerRelationshipIdModel>,
        envelopeCategoryCounts: List<CountPerCategoryIdModel>,
        ledgerCategoryCounts: List<CountPerCategoryIdModel>,
    ): SusuBasicEnvelopeStatistic {
        // 최근 사용 금액
        val envelopHandOverAtMonthCountModel = envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
            ?.map { count ->
                TitleValueModel(count.handedOverAtMonth.toString(), count.totalCounts)
            }?.sortedBy { model -> model.title }

        // 경조사비를 가장 많이 쓴 달
        val mostSpentMonth = envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.handedOverAtMonth
            ?.toLong()

        // 최다 수수 관계
        val relationShipIdConutModel = relationShipConuts.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.run {
                TitleValueModel(
                    title = relationshipService.getRelationship(this.relationshipId).relation,
                    value = this.totalCounts
                )
            }

        // 최다 수수 경조사
        val categoryIdSet = envelopeCategoryCounts.map { count -> count.categoryId }.toSet()
            .union(ledgerCategoryCounts.map { count -> count.categoryId })
        val categoryCounts = categoryIdSet.map { id ->
            val envelopeCount = envelopeCategoryCounts.firstOrNull { it.categoryId == id }
                ?.totalCounts ?: 0L
            val ledgerCount = ledgerCategoryCounts.firstOrNull { it.categoryId == id }
                ?.totalCounts ?: 0L
            CountPerCategoryIdModel(
                categoryId = id,
                totalCounts = envelopeCount + ledgerCount
            )
        }
        val categotyMaxCountModel = categoryCounts.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.let {
                val category = categoryService.getCategory(it.categoryId)
                TitleValueModel(title = category.name, value = it.totalCounts)
            }

        return SusuBasicEnvelopeStatistic(
            recentSpent = envelopHandOverAtMonthCountModel,
            mostSpentMonth = mostSpentMonth,
            relationship = relationShipIdConutModel,
            category = categotyMaxCountModel
        )
    }

    suspend fun getStatisticOrThrow(): SusuBasicEnvelopeStatistic {
        return getStatisticOrNull() ?: throw FailToExecuteException(ErrorCode.NOT_FOUND_SUSU_BASIC_STATISTIC_ERROR)
    }

    suspend fun getStatisticOrNull(): SusuBasicEnvelopeStatistic? {
        return withContext(Dispatchers.IO) { susuBasicEnvelopeStatisticRepository.getStatistic() }
    }

    suspend fun save(susuBasicEnvelopeStatistic: SusuBasicEnvelopeStatistic) {
        withContext(Dispatchers.IO) { susuBasicEnvelopeStatisticRepository.save(susuBasicEnvelopeStatistic) }
    }
}
