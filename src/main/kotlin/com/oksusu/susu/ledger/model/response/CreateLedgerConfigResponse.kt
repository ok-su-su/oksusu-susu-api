package com.oksusu.susu.ledger.model.response

data class CreateLedgerConfigResponse(
    /** 장부 생성시, 시작일 정보만 필요한 categoryIds */
    val onlyStartAtCategoryIds: List<Long>,
)
