package com.oksusu.susu.statistic.infrastructure.redis

import com.oksusu.susu.statistic.domain.SusuBasicStatistic
import org.springframework.data.repository.CrudRepository

interface SusuBasicStatisticRepository : CrudRepository<SusuBasicStatistic, Long>
