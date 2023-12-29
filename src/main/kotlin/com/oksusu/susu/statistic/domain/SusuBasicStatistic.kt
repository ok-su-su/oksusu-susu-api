package com.oksusu.susu.statistic.domain

import com.oksusu.susu.common.consts.SUSU_STATISTIC_TTL
import com.oksusu.susu.statistic.model.SusuBasicStatisticModel
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("susu_basic_statistic")
class SusuBasicStatistic(
    @Id
    var id: Long,

    var statistic: SusuBasicStatisticModel,

    @TimeToLive // TTL
    var ttl: Int,
) {
    companion object {
        fun from(id: Long, statistic: SusuBasicStatisticModel): SusuBasicStatistic {
            return SusuBasicStatistic(
                id = id,
                statistic = statistic,
                ttl = SUSU_STATISTIC_TTL
            )
        }
    }
}
