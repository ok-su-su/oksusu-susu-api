package com.oksusu.susu.post.application

import com.oksusu.susu.post.model.PostCategoryModel
import org.springframework.stereotype.Service

@Service
class PostConfigService(
    private val postCategoryService: PostCategoryService,
) {
    suspend fun getAll(): List<PostCategoryModel> {
        return postCategoryService.getAll()
    }
}
