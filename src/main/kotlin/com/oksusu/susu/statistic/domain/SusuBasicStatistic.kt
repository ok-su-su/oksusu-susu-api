package com.oksusu.susu.statistic.domain

import com.oksusu.susu.statistic.model.SusuBasicStatisticModel
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash

@RedisHash("susu_basic_statistic")
class SusuBasicStatistic(
    @Id
    var id: Long,

    var statistic: SusuBasicStatisticModel,
) {
    companion object {
        fun from(id: Long, statistic: SusuBasicStatisticModel): SusuBasicStatistic {
            return SusuBasicStatistic(
                id = id,
                statistic = statistic
            )
        }
    }
}
