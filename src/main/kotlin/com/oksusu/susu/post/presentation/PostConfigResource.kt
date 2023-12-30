package com.oksusu.susu.post.presentation

import com.oksusu.susu.auth.model.AuthUser
import com.oksusu.susu.extension.wrapOk
import com.oksusu.susu.post.application.PostCategoryService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = "게시글 Config")
@RestController
@RequestMapping(value = ["/api/v1/posts/configs"], produces = [MediaType.APPLICATION_JSON_VALUE])
class PostConfigResource(
    private val postCategoryService: PostCategoryService,
) {
    @Operation(summary = "게시글 카테고리 데이터 제공")
    @GetMapping
    suspend fun getCreateEnvelopesConfig(
        user: AuthUser,
    ) = postCategoryService.getAll().wrapOk()
}
