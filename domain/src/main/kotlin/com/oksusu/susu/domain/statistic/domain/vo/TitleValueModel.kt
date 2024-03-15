package com.oksusu.susu.domain.statistic.domain.vo

data class TitleValueModel<VALUE_TYPE>(
    var title: String,
    var value: VALUE_TYPE,
)
