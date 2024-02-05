package com.oksusu.susu.statistic.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.FailToExecuteException
import com.oksusu.susu.statistic.domain.SusuEnvelopeStatistic
import com.oksusu.susu.statistic.infrastructure.redis.SusuEnvelopeStatisticRepository
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        return withContext(Dispatchers.IO) { susuEnvelopeStatisticRepository.getStatistic() }
    }

    suspend fun save(susuEnvelopeStatistic: SusuEnvelopeStatistic) {
        withContext(Dispatchers.IO) { susuEnvelopeStatisticRepository.save(susuEnvelopeStatistic) }
    }
}
