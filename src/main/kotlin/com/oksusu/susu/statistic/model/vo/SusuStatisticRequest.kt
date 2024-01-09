package com.oksusu.susu.statistic.model.vo

data class SusuStatisticRequest(
    val age: AgeType,
    val relationshipId: Long,
    val postCategoryId: Long,
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
