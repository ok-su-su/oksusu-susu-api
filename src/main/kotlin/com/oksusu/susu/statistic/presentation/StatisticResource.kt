package com.oksusu.susu.statistic.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.statistic.application.StatisticFacade
import com.oksusu.susu.statistic.model.vo.SusuStatisticRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "통계")
@RestController
@RequestMapping(value = ["/api/v1/statistics"], produces = [MediaType.APPLICATION_JSON_VALUE])
class StatisticResource(
    private val statisticFacade: StatisticFacade,
) {
    @Operation(summary = "나의 통계")
    @GetMapping("/mine")
    suspend fun getUserStatistic(
        user: AuthUser,
    ) = statisticFacade.getUserStatistic(user).wrapOk()

    @Operation(summary = "수수 통계")
    @GetMapping("/susu")
    suspend fun getSusuStatistic(
        user: AuthUser,
        @ParameterObject susuStatisticRequest: SusuStatisticRequest,
    ) = statisticFacade.getSusuStatistic(susuStatisticRequest).wrapOk()
}
