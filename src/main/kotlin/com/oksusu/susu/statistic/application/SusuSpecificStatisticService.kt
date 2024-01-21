package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.cache.helper.CacheKeyGenerateHelper
import com.oksusu.susu.envelope.infrastructure.model.CountAvgAmountPerStatisticGroupModel
import com.oksusu.susu.extension.toAgeGroup
import com.oksusu.susu.statistic.infrastructure.redis.SusuSpecificStatisticRepository
import com.oksusu.susu.statistic.model.SusuSpecificStatisticModel
import com.oksusu.susu.statistic.model.TitleStringModel
import com.oksusu.susu.statistic.model.vo.SusuStatisticRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service

@Service
class SusuSpecificStatisticService(
    private val susuSpecificStatisticRepository: SusuSpecificStatisticRepository,
    private val cacheKeyGenerateHelper: CacheKeyGenerateHelper,
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun getSusuSpecificStatistic(request: SusuStatisticRequest): SusuSpecificStatisticModel {
        val ageCategoryRelationshipKey = cacheKeyGenerateHelper.getSusuSpecificStatisticKey(
            age = request.age.number,
            postCategoryId = request.postCategoryId,
            relationshipId = request.relationshipId
        )

        return parZip(
            { findByKey(ageCategoryRelationshipKey) },
            { findByKey(cacheKeyGenerateHelper.getSusuCategoryStatisticKey(request.postCategoryId)) },
            { findByKey(cacheKeyGenerateHelper.getSusuRelationshipStatisticKey(request.relationshipId)) }
        ) { averageSent, categoryAmount, relationShipAmount ->
            SusuSpecificStatisticModel(
                averageSent = averageSent,
                averageRelationship = relationShipAmount?.let {
                    TitleStringModel(
                        title = "temp",
                        value = relationShipAmount
                    )
                },
                averageCategory = categoryAmount?.let { TitleStringModel(title = "temp", value = categoryAmount) }
            )
        }
    }

    suspend fun save(model: CountAvgAmountPerStatisticGroupModel) {
        val key = cacheKeyGenerateHelper.getSusuSpecificStatisticKey(
            age = model.birth.toAgeGroup(),
            postCategoryId = model.categoryId,
            relationshipId = model.relationshipId
        )

        withContext(Dispatchers.IO) {
            susuSpecificStatisticRepository.save(
                key = key,
                value = model.averageAmount.toString()
            )
        }
    }

    suspend fun save(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            susuSpecificStatisticRepository.save(key, value.toString())
        }
    }

    suspend fun findByKey(key: String): String? {
        return withContext(Dispatchers.IO) { susuSpecificStatisticRepository.findByKey(key) }
    }
}
