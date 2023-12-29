package com.oksusu.susu.statistic.application

import com.oksusu.susu.extension.toClockEpochMilli
import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import com.oksusu.susu.statistic.infrastructure.redis.SusuBasicStatisticRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SusuBasicStatisticService(
    private val susuBasicStatisticRepository: SusuBasicStatisticRepository,
) {
    val logger = mu.KotlinLogging.logger { }

    suspend fun getLatestSusuBasicStatistic(): SusuBasicStatistic {
        val id = LocalDateTime.now().toClockEpochMilli()
        return findByIdOrThrow(id)
    }

    suspend fun findByIdOrThrow(id: Long): SusuBasicStatistic {
        // 통계 없으면 에러 띄우기
        return findByIdOrNull(id)!!
    }

    suspend fun findByIdOrNull(id: Long): SusuBasicStatistic? {
        return withContext(Dispatchers.IO) {
            susuBasicStatisticRepository.findByIdOrNull(id)
        }
    }

    suspend fun save(susuBasicStatistic: SusuBasicStatistic) {
        withContext(Dispatchers.IO) {
            susuBasicStatisticRepository.save(susuBasicStatistic)
        }
    }
}
