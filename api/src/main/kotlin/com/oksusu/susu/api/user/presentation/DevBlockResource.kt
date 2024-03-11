package com.oksusu.susu.api.user.presentation

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.common.extension.wrapVoid
import com.oksusu.susu.api.user.application.BlockFacade
import com.oksusu.susu.api.user.model.request.DeleteBlockRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.DEV_BLOCK_SWAGGER_TAG, description = "개발용 차단 API")
@RestController
@RequestMapping(value = ["/api/v1/dev/blocks"], produces = [MediaType.APPLICATION_JSON_VALUE])
class DevBlockResource(
    private val blockFacade: BlockFacade,
) {
    @Operation(tags = [SwaggerTag.DEV_SWAGGER_TAG], summary = "차단 해제하기")
    @DeleteMapping
    suspend fun deleteBlockByTargetId(
        user: AuthUser,
        @RequestBody request: DeleteBlockRequest,
    ) = blockFacade.deleteBlockByTargetId(user, request).wrapVoid()
}
