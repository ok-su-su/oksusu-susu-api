package com.oksusu.susu.block.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.block.application.BlockFacade
import com.oksusu.susu.block.model.request.CreateBlockRequest
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapCreated
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.*

@Tag(name = SwaggerTag.BLOCK_SWAGGER_TAG)
@RestController
@RequestMapping(value = ["/api/v1/blocks"], produces = [MediaType.APPLICATION_JSON_VALUE])
class BlockResource(
    private val blockFacade: BlockFacade,
) {
    @Operation(summary = "차단하기")
    @PostMapping
    suspend fun createBlock(
        user: AuthUser,
        @RequestBody request: CreateBlockRequest,
    ) = blockFacade.createBlock(user, request).wrapCreated()
}
