package com.oksusu.susu.extension

import com.oksusu.susu.exception.BusinessException
import com.oksusu.susu.exception.ErrorCode
import com.querydsl.jpa.impl.JPAQuery
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.support.Querydsl

fun <T> Querydsl?.execute(query: JPAQuery<T>, pageable: Pageable): Page<T> {
    return this.takeUnless { querydsl -> querydsl == null }
        ?.let { queryDsl ->
            queryDsl.applyPagination(pageable, query).run {
                PageImpl(this.fetch(), pageable, this.fetchCount())
            }
        } ?: throw BusinessException(ErrorCode.QUERY_DSL_NOT_EXISTS_ERROR)
}
