package com.oksusu.susu.statistic.domain

import com.oksusu.susu.statistic.model.response.UserStatisticResponse
import org.springframework.data.annotation.Id
import org.springframework.data.redis.core.RedisHash
import org.springframework.data.redis.core.TimeToLive

@RedisHash("user_statistic")
class UserStatistic(
    @Id
    var id: Long,

    var userStatisticResponse: UserStatisticResponse,

    @TimeToLive // TTL
    var ttl: Int,
) {
    companion object {
        private const val USER_STATISTIC_TTL = 180

        fun from(id: Long, userStatisticResponse: UserStatisticResponse): UserStatistic {
            return UserStatistic(
                id = id,
                userStatisticResponse = userStatisticResponse,
                ttl = USER_STATISTIC_TTL
            )
        }
    }
}
