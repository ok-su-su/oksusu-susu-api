package com.oksusu.susu.api.post.presentation

import com.oksusu.susu.api.auth.model.AdminUser
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.domain.common.extension.wrapVoid
import com.oksusu.susu.api.post.application.PostAdminFacade
import com.oksusu.susu.domain.post.domain.vo.PostType
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.ADMIN_VOTE_SWAGGER_TAG, description = "어드민용 게시글 API")
@RestController
@RequestMapping(value = ["/api/v1/admin/posts"], produces = [MediaType.APPLICATION_JSON_VALUE])
class VoteAdminResource(
    private val postAdminFacade: PostAdminFacade,
) {
    /**
     * 어드민 유저만 실행 가능합니다.
     */
    @Operation(summary = "게시글 삭제하기", tags = [SwaggerTag.ADMIN_SWAGGER_TAG])
    @DeleteMapping("/{id}")
    suspend fun deletePost(
        user: AdminUser,
        @RequestParam type: PostType,
        @PathVariable id: Long,
    ) = postAdminFacade.deletePost(user, type, id).wrapVoid()
}
