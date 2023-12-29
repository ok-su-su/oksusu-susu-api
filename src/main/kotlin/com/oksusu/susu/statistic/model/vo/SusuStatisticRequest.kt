package com.oksusu.susu.statistic.model.vo

class SusuStatisticRequest(
    val age: AgeType,
    val relationship: Long,
    val category: Long,
)

enum class AgeType(
    val number: Long,
) {
    TEN(10),
    TWENTY(20),
    THIRTY(30),
    FOURTY(40),
    FIFTY(50),
    SIXTY(60),
    SEVENTY(70),
    EIGHTY(80),
    NINETY(90),
    ;
}
