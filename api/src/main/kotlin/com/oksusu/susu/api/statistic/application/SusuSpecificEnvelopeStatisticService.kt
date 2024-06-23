package com.oksusu.susu.api.statistic.application

import com.oksusu.susu.api.statistic.model.SusuSpecificEnvelopeStatisticModel
import com.oksusu.susu.api.statistic.model.vo.SusuEnvelopeStatisticRequest
import com.oksusu.susu.cache.key.CacheKeyGenerateHelper
import com.oksusu.susu.cache.statistic.infrastructure.SusuSpecificEnvelopeStatisticRepository
import com.oksusu.susu.common.extension.parZipWithMDC
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.common.model.TitleValueModel
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class SusuSpecificEnvelopeStatisticService(
    private val susuSpecificEnvelopeStatisticRepository: SusuSpecificEnvelopeStatisticRepository,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getStatistic(request: SusuEnvelopeStatisticRequest): SusuSpecificEnvelopeStatisticModel {
        val ageCategoryRelationshipKey = CacheKeyGenerateHelper.getSusuSpecificStatisticKey(
            age = request.age.number,
            categoryId = request.categoryId,
            relationshipId = request.relationshipId
        )
        val categoryKey = CacheKeyGenerateHelper.getSusuCategoryStatisticKey(request.categoryId)
        val relationshipKey = CacheKeyGenerateHelper.getSusuRelationshipStatisticKey(request.relationshipId)

        return parZipWithMDC(
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

    suspend fun save(key: String, value: Long) {
        withMDCContext(Dispatchers.IO) {
            susuSpecificEnvelopeStatisticRepository.save(key, value)
        }
    }

    suspend fun findByKey(key: String): Long? {
        return withMDCContext(Dispatchers.IO) { susuSpecificEnvelopeStatisticRepository.findByKey(key) }
    }
}
