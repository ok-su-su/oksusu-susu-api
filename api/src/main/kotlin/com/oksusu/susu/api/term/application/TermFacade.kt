package com.oksusu.susu.api.term.application

import com.oksusu.susu.api.term.model.response.GetTermInfosResponse
import com.oksusu.susu.api.term.model.response.GetTermResponse
import org.springframework.stereotype.Service

@Service
class TermFacade(
    private val termService: TermService,
) {
    suspend fun getTerm(id: Long): GetTermResponse {
        return termService.findByIdOrThrow(id)
            .run { GetTermResponse.from(this) }
    }

    suspend fun getTermInfos(): List<GetTermInfosResponse> {
        return termService.getAllActiveTerms()
            .map { term -> GetTermInfosResponse.from(term) }
    }
}
