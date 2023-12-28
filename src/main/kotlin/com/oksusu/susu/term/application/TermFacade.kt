package com.oksusu.susu.term.application

import com.oksusu.susu.term.model.response.GetTermInfosResponse
import com.oksusu.susu.term.model.response.GetTermResponse
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class TermFacade(
    private val termService: TermService,
) {
    @Transactional(readOnly = true)
    suspend fun getTerm(id: Long): GetTermResponse {
        return termService.findByIdOrThrow(id).run { GetTermResponse.from(this) }
    }

    @Transactional(readOnly = true)
    suspend fun getTermInfos(): List<GetTermInfosResponse> {
        return termService.getAllActiveTerms().map { GetTermInfosResponse.from(it) }
    }
}
