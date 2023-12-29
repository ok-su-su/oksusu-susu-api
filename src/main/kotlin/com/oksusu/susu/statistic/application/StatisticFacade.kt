package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.category.application.CategoryService
import com.oksusu.susu.envelope.application.EnvelopeService
import com.oksusu.susu.envelope.domain.vo.EnvelopeType
import com.oksusu.susu.envelope.infrastructure.model.CountPerCategoryIdModel
import com.oksusu.susu.envelope.infrastructure.model.CountPerHandedOverAtModel
import com.oksusu.susu.extension.toClockEpochMilli
import com.oksusu.susu.friend.application.FriendRelationshipService
import com.oksusu.susu.friend.application.RelationshipService
import com.oksusu.susu.friend.infrastructure.model.CountPerRelationshipIdModel
import com.oksusu.susu.ledger.application.LedgerService
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import com.oksusu.susu.statistic.domain.UserStatistic
import com.oksusu.susu.statistic.model.SusuBasicStatisticModel
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.response.SusuStatisticResponse
import com.oksusu.susu.statistic.model.response.UserStatisticResponse
import com.oksusu.susu.statistic.model.vo.SusuStatisticRequest
import kotlinx.coroutines.*
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class StatisticFacade(
    private val envelopeService: EnvelopeService,
    private val friendRelationshipService: FriendRelationshipService,
    private val ledgerService: LedgerService,
    private val categoryService: CategoryService,
    private val userStatisticService: UserStatisticService,
    private val susuBasicStatisticService: SusuBasicStatisticService,
    private val susuSpecificStatisticService: SusuSpecificStatisticService,
    private val relationshipService: RelationshipService,
) {
    val logger = mu.KotlinLogging.logger { }

    @Scheduled(
        fixedRate = 1000 * 60 * 60,
        initialDelayString = "\${oksusu.scheduled-tasks.refresh-susu-statistic.initial-delay:100}"
    )
    fun refreshSusuStatistic() {
        logger.info { "start susu statistic refresh" }

        CoroutineScope(Dispatchers.IO).launch {
            parZip(
                // 최근 사용 금액
                // 경조사비를 가장 많이 쓴 달
                { envelopeService.countPerHandedOverAtInLast8Month(EnvelopeType.SENT) },
                // 최다 수수 관계
                { friendRelationshipService.countPerRelationshipId() },
                // 최다 수수 경조사
                { envelopeService.countPerCategoryId() },
                { ledgerService.countPerCategoryId() },
                // 평균 수수
                { envelopeService.countAvgAmountPerCategoryIdAndRelationshipIdAndBirth() }
            ) {
                    envelopHandOverAtMonthCount,
                    relationShipConuts,
                    envelopeCategoryCounts,
                    ledgerCategoryCounts,
                    avgAmountModels,
                ->

                // 최근 사용 금액 + 경조사비를 가장 많이 쓴 달 + 최다 수수 관계 + 최다 수수 경조사 레디스 저장
                susuBasicStatisticService.save(
                    constructBasicStatistic(
                        envelopHandOverAtMonthCount,
                        relationShipConuts,
                        envelopeCategoryCounts,
                        ledgerCategoryCounts
                    )
                )

                // 평균 수수 레디스 저장
                // key: age_categoryId_relationshipId, value: avg
                avgAmountModels.map { avgAmountModel ->
                    async { susuSpecificStatisticService.save(avgAmountModel) }
                }

                // key: category_categoryId, value: avg
                avgAmountModels.groupBy { it.categoryId }.map { modelsMap ->
                    val key = "category_" + modelsMap.key.toString()
                    val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount }
                    async { susuSpecificStatisticService.save(key, avgAmount) }
                }

                // key: relationship_relationshipId, value: avg
                avgAmountModels.groupBy { it.relationshipId }.map { modelsMap ->
                    val key = "relationship_" + modelsMap.key.toString()
                    val avgAmount = modelsMap.value.sumOf { model -> model.averageAmount }
                    async { susuSpecificStatisticService.save(key, avgAmount) }
                }
            }

            logger.info { "end susu statistic refresh" }
        }
    }

    suspend fun getUserStatistic(user: AuthUser): UserStatisticResponse {
        // caching 된거 확인
        userStatisticService.findByIdOrNull(user.id)?.run {
            logger.debug { "${user.id} user statistic cache hit" }
            return this.userStatisticResponse
        }

        val userStatisticResponse = parZip(
            // 최근 사용 금액
            // 경조사비를 가장 많이 쓴 달
            { envelopeService.countPerHandedOverAtInLast8MonthByUid(user.id, EnvelopeType.SENT) },
            // 최다 수수 관계
            { friendRelationshipService.countPerRelationshipIdByUid(user.id) },
            // 최다 수수 경조사
            { envelopeService.countPerCategoryIdByUid(user.id) },
            { ledgerService.countPerCategoryIdByUid(user.id) },
            // 가장 많이 받은 금액
            { envelopeService.getMaxAmountEnvelopeInfoByUid(user.id, EnvelopeType.RECEIVED) },
            // 가장 많이 보낸 금액
            { envelopeService.getMaxAmountEnvelopeInfoByUid(user.id, EnvelopeType.SENT) }
        ) {
                envelopHandOverAtMonthCount,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts,
                receivedMaxAmount,
                sentMaxAmount,
            ->

            // 최근 사용 금액 + 경조사비를 가장 많이 쓴 달 + 최다 수수 관계 + 최다 수수 경조사
            val basicStatistic = constructBasicStatistic(
                envelopHandOverAtMonthCount,
                relationShipConuts,
                envelopeCategoryCounts,
                ledgerCategoryCounts
            )

            // 가장 많이 받은 금액
            val receivedMaxAmountModel =
                receivedMaxAmount?.run { TitleValueModel(title = this.friend.name, value = this.envelope.amount) }

            // 가장 많이 보낸 금액
            val sentMaxAmountModel =
                sentMaxAmount?.run { TitleValueModel(title = this.friend.name, value = this.envelope.amount) }

            UserStatisticResponse.of(
                basicStatistic,
                receivedMaxAmountModel,
                sentMaxAmountModel
            )
        }

        UserStatistic.from(user.id, userStatisticResponse).run { userStatisticService.save(this) }

        return userStatisticResponse
    }

    suspend fun constructBasicStatistic(
        envelopHandOverAtMonthCount: List<CountPerHandedOverAtModel>,
        relationShipConuts: List<CountPerRelationshipIdModel>,
        envelopeCategoryCounts: List<CountPerCategoryIdModel>,
        ledgerCategoryCounts: List<CountPerCategoryIdModel>,
    ): SusuBasicStatistic {
        // 최근 사용 금액
        val envelopHandOverAtMonthCountModel = envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
            ?.map { count -> TitleValueModel(count.handedOverAtMonth.toString(), count.totalCounts) }

        // 경조사비를 가장 많이 쓴 달
        val mostSpentMonth = envelopHandOverAtMonthCount.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.handedOverAtMonth!!.toLong()

        // 최다 수수 관계
        val relationShipIdConutModel = relationShipConuts.takeIf { it.isNotEmpty() }
            ?.maxBy { it.totalCounts }
            ?.run {
                TitleValueModel(
                    title = this.relationship.relation,
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

        return SusuBasicStatistic.from(
            id = LocalDateTime.now().toClockEpochMilli(),
            statistic = SusuBasicStatisticModel(
                recentSpent = envelopHandOverAtMonthCountModel,
                mostSpentMonth = mostSpentMonth,
                relationship = relationShipIdConutModel,
                category = categotyMaxCountModel
            )
        )
    }

    suspend fun getSusuStatistic(requestParam: SusuStatisticRequest): SusuStatisticResponse {
        return parZip(
            { susuSpecificStatisticService.getLatestSusuSpecificStatistic(requestParam) },
            { susuBasicStatisticService.getLatestSusuBasicStatistic() }
        ) { tempSpecific, basic ->
            val specific = tempSpecific.apply {
                this.averageCategory?.apply { title = categoryService.getCategory(requestParam.category).name }
                this.averageRelationship?.apply {
                    title =
                        relationshipService.getRelationship(requestParam.relationship).relation
                }
            }
            SusuStatisticResponse.of(specific, basic.statistic)
        }
    }
}
