package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.extension.toAgeGroup
import com.oksusu.susu.statistic.infrastructure.redis.SusuSpecificStatisticRepository
import com.oksusu.susu.statistic.model.SusuSpecificStatisticModel
import com.oksusu.susu.statistic.model.TitleValueModel
import com.oksusu.susu.statistic.model.vo.SusuStatisticRequest
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class SusuSpecificStatisticService(
    private val susuSpecificStatisticRepository: SusuSpecificStatisticRepository,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getSusuSpecificStatistic(request: SusuStatisticRequest): SusuSpecificStatisticModel {
        val ageCategoryRelationshipKey = cacheKeyGenerateHelper.getSusuSpecificStatisticKey(
            age = request.age.number,
            categoryId = request.categoryId,
            relationshipId = request.relationshipId
        )

        return parZip(
            { findByKey(ageCategoryRelationshipKey) },
            { findByKey(cacheKeyGenerateHelper.getSusuCategoryStatisticKey(request.categoryId)) },
            { findByKey(cacheKeyGenerateHelper.getSusuRelationshipStatisticKey(request.relationshipId)) }
        ) { averageSent, categoryAmount, relationShipAmount ->
            SusuSpecificStatisticModel(
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
            susuSpecificStatisticRepository.save(
                key = key,
                value = model.averageAmount
            )
        }
    }

    suspend fun save(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            susuSpecificStatisticRepository.save(key, value)
        }
    }

    suspend fun findByKey(key: String): Long? {
        return withContext(Dispatchers.IO) { susuSpecificStatisticRepository.findByKey(key) }
    }
}
