package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
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
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun getSusuSpecificStatistic(request: SusuStatisticRequest): SusuSpecificStatisticModel {
        val ageCategoryRelationshipKey = generateStatisticKey(
            age = request.age.number,
            postCategoryId = request.postCategoryId,
            relationshipId = request.relationshipId
        )

        return parZip(
            { findByKey(ageCategoryRelationshipKey) },
            { findByKey("category_" + request.postCategoryId.toString()) },
            { findByKey("relationship_" + request.relationshipId.toString()) }
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
        val key = generateStatisticKey(
            model.birth.toAgeGroup(),
            model.categoryId,
            model.relationshipId
        )
        withContext(Dispatchers.IO) {
            susuSpecificStatisticRepository.save(key, model.averageAmount.toString(), SUSU_STATISTIC_TTL)
        }
    }

    suspend fun save(key: String, value: Long) {
        withContext(Dispatchers.IO) {
            susuSpecificStatisticRepository.save(key, value.toString(), SUSU_STATISTIC_TTL)
        }
    }

    suspend fun findByKey(key: String): String? {
        return withContext(Dispatchers.IO) {
            susuSpecificStatisticRepository.findByKey(key)
        }
    }

    suspend fun generateStatisticKey(age: Long, postCategoryId: Long, relationshipId: Long): String {
        return age.toString() + "_" + postCategoryId.toString() + "_" + relationshipId.toString()
    }
}
