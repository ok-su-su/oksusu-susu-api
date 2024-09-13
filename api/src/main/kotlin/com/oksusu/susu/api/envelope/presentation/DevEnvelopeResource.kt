package com.oksusu.susu.api.envelope.presentation

import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.common.dto.SusuPageRequest
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.envelope.application.EnvelopeFacade
import com.oksusu.susu.api.envelope.model.request.DevSearchEnvelopeRequest
import com.oksusu.susu.api.extension.wrapPage
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.annotations.ParameterObject
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.DEV_ENVELOPE_SWAGGER_TAG, description = "개발용 Envelope API")
@RestController
@RequestMapping(value = ["/api/v1/dev/envelopes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DevEnvelopeResource(
    private val envelopeFacade: EnvelopeFacade,
) {
    /**
     * **검색조건**
     * - uid: 조회할 대상
     * - types: SENT: 보낸 봉투만, RECEIVED: 받은 봉투만, 그외 케이스는 전체 봉투 정보
     * - friendName: 친구 이름으로 검색, ex) %동건%
     *
     * **정렬조건**
     * - createdAt: 생성일
     * - amount: 봉투 금액
     * - handedOverAt: 전달일
     */
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "봉투 검색")
    @GetMapping
    suspend fun search(
        adminUser: AdminUser,
        @ParameterObject request: DevSearchEnvelopeRequest,
        @ParameterObject pageRequest: SusuPageRequest,
    ) = envelopeFacade.search(
        uid = request.uid,
        request = request.convert(),
        pageRequest = pageRequest
    ).wrapPage()
}
