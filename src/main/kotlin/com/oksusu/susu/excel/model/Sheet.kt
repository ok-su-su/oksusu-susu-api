package com.oksusu.susu.excel.model

abstract class Sheet(
    open val name: String,
    open val titles: Map<String, String>,
)

class SentSheet(
    override val name: String = "보내요",
    override val titles: Map<String, String> = mapOf(
        "날짜" to "date",
        "경조사" to "categoryName",
        "나와의 관계" to "relationship",
        "이름" to "friendName",
        "금액" to "amount",
        "방문여부" to "hasVisited",
        "선물" to "gift",
        "메모" to "memo",
        "연락처" to "phoneNumber"
    ),
) : Sheet(name, titles)

class ReceivedSheet(
    override val name: String = "받아요",
    override val titles: Map<String, String> = mapOf(
        "날짜" to "date",
        "경조사" to "categoryName",
        "나와의 관계" to "relationship",
        "이름" to "friendName",
        "금액" to "amount",
        "방문여부" to "hasVisited",
        "선물" to "gift",
        "메모" to "memo",
        "연락처" to "phoneNumber"
    ),
) : Sheet(name, titles)
