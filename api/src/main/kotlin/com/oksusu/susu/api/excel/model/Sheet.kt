package com.oksusu.susu.api.excel.model

sealed class Sheet(
    open val name: String,
    open val titles: Map<String, String>,
)

class SentSheet(
    override val name: String,
    override val titles: Map<String, String>,
) : Sheet(name, titles) {
    companion object Factory {
        fun getSheet(): Sheet {
            return SentSheet(
                name = "보내요",
                titles = mapOf(
                    "날짜" to "date",
                    "경조사" to "categoryName",
                    "나와의 관계" to "relationship",
                    "이름" to "friendName",
                    "금액" to "amount",
                    "방문여부" to "hasVisited",
                    "선물" to "gift",
                    "메모" to "memo",
                    "연락처" to "phoneNumber"
                )
            )
        }
    }
}

class ReceivedSheet(
    override val name: String,
    override val titles: Map<String, String>,
) : Sheet(name, titles) {
    companion object Factory {
        fun getSheet(): Sheet {
            return ReceivedSheet(
                name = "받아요",
                titles = mapOf(
                    "날짜" to "date",
                    "경조사" to "categoryName",
                    "나와의 관계" to "relationship",
                    "이름" to "friendName",
                    "금액" to "amount",
                    "방문여부" to "hasVisited",
                    "선물" to "gift",
                    "메모" to "memo",
                    "연락처" to "phoneNumber"
                )
            )
        }
    }
}
