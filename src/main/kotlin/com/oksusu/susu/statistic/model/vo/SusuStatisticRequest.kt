package com.oksusu.susu.statistic.model.vo

data class SusuStatisticRequest(
    /** required | 나이 */
    val age: AgeType,
    /** required | 관계 id */
    val relationshipId: Long,
    /** required | 카테고리 id */
    val categoryId: Long,
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
