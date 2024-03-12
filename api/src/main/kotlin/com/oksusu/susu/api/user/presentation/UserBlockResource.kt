package com.oksusu.susu.api.user.presentation

import com.oksusu.susu.api.auth.model.AuthUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.domain.common.extension.wrapCreated
import com.oksusu.susu.domain.common.extension.wrapVoid
import com.oksusu.susu.api.user.application.BlockFacade
import com.oksusu.susu.api.user.model.request.CreateBlockRequest
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.BLOCK_SWAGGER_TAG, description = "차단 API")
@RestController
@RequestMapping(value = ["/api/v1/blocks"], produces = [MediaType.APPLICATION_JSON_VALUE])
class UserBlockResource(
    private val blockFacade: BlockFacade,
) {
    @Operation(summary = "차단하기")
    @PostMapping
    suspend fun createBlock(
        user: AuthUser,
        @RequestBody request: CreateBlockRequest,
    ) = blockFacade.createBlock(user, request).wrapCreated()

    @Operation(summary = "차단 해제하기")
    @DeleteMapping("/{id}")
    suspend fun deleteBlock(
        user: AuthUser,
        @PathVariable id: Long,
    ) = blockFacade.deleteBlock(user, id).wrapVoid()
}
