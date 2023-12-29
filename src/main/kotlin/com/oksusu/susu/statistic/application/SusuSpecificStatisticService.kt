package com.oksusu.susu.statistic.application

import arrow.fx.coroutines.parZip
import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.envelope.infrastructure.model.CountAvgAmountPerCategoryIdAndRelationshipIdAndBirthModel
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

    suspend fun getLatestSusuSpecificStatistic(request: SusuStatisticRequest): SusuSpecificStatisticModel {
        val ageCategoryRelationshipKey = generateStatisticKey(
            request.age.number,
            request.category,
            request.relationship
        )

        return parZip(
            { findByKey(ageCategoryRelationshipKey) },
            { findByKey("category_" + request.category.toString()) },
            { findByKey("relationship_" + request.relationship.toString()) }
        ) { averageSent, categoryAmount, relationShipAmount ->
            logger.info { "$averageSent $categoryAmount $relationShipAmount" }
            val a = SusuSpecificStatisticModel(
                averageSent = averageSent,
                averageRelationship = relationShipAmount?.let {
                    TitleStringModel(
                        title = "temp",
                        value = relationShipAmount
                    )
                },
                averageCategory = categoryAmount?.let { TitleStringModel(title = "temp", value = categoryAmount) }
            )
            logger.info { a.averageCategory.toString() }
            a
        }
    }

    suspend fun save(model: CountAvgAmountPerCategoryIdAndRelationshipIdAndBirthModel) {
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

    suspend fun generateStatisticKey(age: Long, category: Long, relationship: Long): String {
        return age.toString() + "_" + category.toString() + "_" + relationship.toString()
    }
}
