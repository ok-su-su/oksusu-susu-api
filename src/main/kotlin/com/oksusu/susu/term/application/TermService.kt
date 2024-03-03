package com.oksusu.susu.term.application

import com.oksusu.susu.exception.ErrorCode
import com.oksusu.susu.exception.InvalidRequestException
import com.oksusu.susu.extension.withMDCContext
import com.oksusu.susu.term.domain.Term
import com.oksusu.susu.term.infrastructure.TermRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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
        return withContext(Dispatchers.IO.withMDCContext()) {
            termRepository.findByIdOrNull(id)
        }
    }

    suspend fun getAllActiveTerms(): List<Term> {
        return withContext(Dispatchers.IO.withMDCContext()) {
            termRepository.findAllByIsActiveOrderByIsEssentialDesc(true)
        }
    }

    suspend fun validateExistTerms(ids: List<Long>) {
        withContext(Dispatchers.IO.withMDCContext()) {
            termRepository.countAllByIdIn(ids)
        }.takeIf { count -> count == ids.size.toLong() }
            ?: throw InvalidRequestException(ErrorCode.NOT_FOUND_TERM_ERROR)
    }
}
