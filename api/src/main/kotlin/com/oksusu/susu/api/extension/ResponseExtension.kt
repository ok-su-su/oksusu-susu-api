package com.oksusu.susu.api.extension

import com.oksusu.susu.api.common.dto.PageResponseDto
import com.oksusu.susu.api.common.dto.SliceResponseDto
import org.springframework.data.domain.Page
import org.springframework.data.domain.Slice
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

/** Wrap Response Slice */
fun <T> Slice<T>.wrapSlice() = SliceResponseDto(this)

/** Wrap Response Page */
fun <T> Page<T>.wrapPage() = PageResponseDto(this)

/** Wrap Response Ok */
fun <T> T.wrapOk() = ResponseEntity.ok(this)

/** Wrap Response Created */
fun <T> T.wrapCreated() = ResponseEntity.status(HttpStatus.CREATED).body(this)

/** Wrap Response Void */
fun Unit.wrapVoid() = ResponseEntity.noContent().build<Unit>()
