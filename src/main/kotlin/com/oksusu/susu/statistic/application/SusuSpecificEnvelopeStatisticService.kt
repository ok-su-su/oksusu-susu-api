package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.extension.toAgeGroup
import com.oksusu.susu.statistic.infrastructure.redis.SusuSpecificEnvelopeStatisticRepository
import com.oksusu.susu.statistic.model.SusuSpecificEnvelopeStatisticModel
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.vo.SusuEnvelopeStatisticRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class SusuSpecificEnvelopeStatisticService(
    private val susuSpecificEnvelopeStatisticRepository: SusuSpecificEnvelopeStatisticRepository,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getStatistic(request: SusuEnvelopeStatisticRequest): SusuSpecificEnvelopeStatisticModel {
        val ageCategoryRelationshipKey = cacheKeyGenerateHelper.getSusuSpecificStatisticKey(
            age = request.age.number,
            categoryId = request.categoryId,
            relationshipId = request.relationshipId
        )
        val categoryKey = cacheKeyGenerateHelper.getSusuCategoryStatisticKey(request.categoryId)
        val relationshipKey = cacheKeyGenerateHelper.getSusuRelationshipStatisticKey(request.relationshipId)

        return parZip(
            { findByKey(ageCategoryRelationshipKey) },
            { findByKey(categoryKey) },
            { findByKey(relationshipKey) }
        ) { averageSent, categoryAmount, relationShipAmount ->
            SusuSpecificEnvelopeStatisticModel(
                averageSent = averageSent,
                averageRelationship = relationShipAmount?.let {
                    TitleValueModel(
                        title = "temp",
                        value = relationShipAmount
                    )
                },
                averageCategory = categoryAmount?.let { TitleValueModel(title = "temp", value = categoryAmount) }
            )
        }
    }

    suspend fun save(model: CountAvgAmountPerStatisticGroupModel) {
        val key = cacheKeyGenerateHelper.getSusuSpecificStatisticKey(
            age = model.birth.toAgeGroup(),
            categoryId = model.categoryId,
            relationshipId = model.relationshipId
        )

        withContext(Dispatchers.IO) {
            susuSpecificEnvelopeStatisticRepository.save(
                key = key,
                value = model.averageAmount
            )
        }
    }

    suspend fun save(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            susuSpecificEnvelopeStatisticRepository.save(key, value)
        }
    }

    suspend fun findByKey(key: String): Long? {
        return withContext(Dispatchers.IO) { susuSpecificEnvelopeStatisticRepository.findByKey(key) }
    }
}
