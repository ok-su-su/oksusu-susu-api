package com.oksusu.susu.post.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.config.web.SwaggerTag
import com.oksusu.susu.extension.wrapVoid
import com.oksusu.susu.post.application.VoteAdminFacade
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.ADMIN_VOTE_SWAGGER_TAG, description = "어드민용 투표 API")
@RestController
@RequestMapping(value = ["/api/v1/admin/votes"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VoteAdminResource(
    private val voteAdminFacade: VoteAdminFacade,
) {
    @Operation(summary = "투표 삭제하기")
    @DeleteMapping("/{id}")
    suspend fun deleteVote(
        user: AuthUser,
        @PathVariable id: Long,
    ) = voteAdminFacade.deleteVote(user, id).wrapVoid()
}
