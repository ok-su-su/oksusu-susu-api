package com.oksusu.susu.api.category.presentation

import com.oksusu.susu.api.category.application.CategoryService
import com.oksusu.susu.api.config.web.SwaggerTag
import com.oksusu.susu.api.extension.wrapOk
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@Tag(name = SwaggerTag.CATEGORY_SWAGGER_TAG, description = "카테고리 관리")
@RestController
@RequestMapping(value = ["/api/v1/categories"], produces = [MediaType.APPLICATION_JSON_VALUE])
class CategoryResource(
    private val categoryService: CategoryService,
) {
    @Operation(summary = "카테고리 전체 조회, 활성화된 정보만 제공")
    @GetMapping
    suspend fun getCategories() = categoryService.getAllByActive().wrapOk()
}
