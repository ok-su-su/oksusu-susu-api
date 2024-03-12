package com.oksusu.susu.api.statistic.presentation

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.domain.common.extension.wrapOk
import com.oksusu.susu.api.statistic.application.StatisticFacade
import com.oksusu.susu.api.statistic.model.vo.SusuEnvelopeStatisticRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.STATISTIC_SWAGGER_TAG, description = "통계 API")
@RestController
@RequestMapping(value = ["/api/v1/statistics"], produces = [MediaType.APPLICATION_JSON_VALUE])
class StatisticResource(
    private val statisticFacade: StatisticFacade,
) {
    @Operation(summary = "나의 통계")
    @GetMapping("/mine/envelope")
    suspend fun getUserEnvelopeStatistic(
        user: AuthUser,
    ) = statisticFacade.getUserEnvelopeStatistic(user).wrapOk()

    @Operation(summary = "수수 통계")
    @GetMapping("/susu/envelope")
    suspend fun getSusuEnvelopeStatistic(
        user: AuthUser,
        @ParameterObject susuStatisticRequest: SusuEnvelopeStatisticRequest,
    ) = statisticFacade.getSusuEnvelopeStatistic(susuStatisticRequest).wrapOk()
}
