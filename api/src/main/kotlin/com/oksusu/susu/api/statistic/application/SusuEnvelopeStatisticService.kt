package com.oksusu.susu.api.statistic.application

import com.oksusu.susu.cache.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.cache.statistic.infrastructure.SusuEnvelopeStatisticRepository
import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.FailToExecuteException
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import org.springframework.stereotype.Service

@Service
class SusuEnvelopeStatisticService(
    private val susuEnvelopeStatisticRepository: SusuEnvelopeStatisticRepository,
) {
    val logger = KotlinLogging.logger { }

    suspend fun getStatisticOrThrow(): SusuEnvelopeStatistic {
        return getStatisticOrNull() ?: throw FailToExecuteException(ErrorCode.NOT_FOUND_SUSU_STATISTIC_ERROR)
    }

    suspend fun getStatisticOrNull(): SusuEnvelopeStatistic? {
        return withMDCContext(Dispatchers.IO) { susuEnvelopeStatisticRepository.getStatistic() }
    }

    suspend fun save(susuEnvelopeStatistic: SusuEnvelopeStatistic) {
        withMDCContext(Dispatchers.IO) { susuEnvelopeStatisticRepository.save(susuEnvelopeStatistic) }
    }
}
