package com.oksusu.susu.statistic.model

data class TitleValueModel<VALUE_TYPE>(
    var title: String,
    var value: VALUE_TYPE,
)
