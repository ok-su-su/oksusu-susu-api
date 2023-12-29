package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.statistic.domain.UserStatistic
import org.springframework.data.repository.CrudRepository

interface UserStatisticRepository : CrudRepository<UserStatistic, Long>
