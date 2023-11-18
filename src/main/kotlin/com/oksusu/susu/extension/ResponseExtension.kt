package com.oksusu.susu.extension

import com.oksusu.susu.common.dto.PageResponseDto
import org.springframework.data.domain.Page
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/** Wrap Response Page */
fun <T> Page<T>.wrapPage() = PageResponseDto(this)

/** Wrap Response Ok */
fun <T> T.wrapOk() = ResponseEntity.ok(this)

/** Wrap Response Created */
fun <T> T.wrapCreated() = ResponseEntity.status(HttpStatus.CREATED).body(this)

/** Wrap Response Void */
fun Unit.wrapVoid() = ResponseEntity.noContent()
