package com.oksusu.susu.api.term.application

import com.oksusu.susu.common.exception.ErrorCode
import com.oksusu.susu.common.exception.InvalidRequestException
import com.oksusu.susu.common.extension.withMDCContext
import com.oksusu.susu.domain.term.domain.Term
import com.oksusu.susu.domain.term.infrastructure.TermRepository
import kotlinx.coroutines.Dispatchers
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service

@Service
class TermService(
    private val termRepository: TermRepository,
) {
    suspend fun findByIdOrThrow(id: Long): Term {
        return findByIdOrNull(id) ?: throw InvalidRequestException(ErrorCode.NOT_FOUND_TERM_ERROR)
    }

    suspend fun findByIdOrNull(id: Long): Term? {
        return withMDCContext(Dispatchers.IO) {
            termRepository.findByIdOrNull(id)
        }
    }

    suspend fun getAllActiveTerms(): List<Term> {
        return withMDCContext(Dispatchers.IO) {
            termRepository.findAllByIsActive(true)
        }.sortedBy { term -> term.seq }
    }

    suspend fun validateExistTerms(ids: List<Long>) {
        withMDCContext(Dispatchers.IO) {
            termRepository.countAllByIdIn(ids)
        }.takeIf { count -> count == ids.size.toLong() }
            ?: throw InvalidRequestException(ErrorCode.NOT_FOUND_TERM_ERROR)
    }
}
