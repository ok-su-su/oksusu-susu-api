package com.oksusu.susu.common.extension

fun Map<Long, Long>.merge(map: Map<Long, Long>): Map<Long, Long> {
    val resultMap = mutableMapOf<Long, Long>()

    for ((key, value) in this) {
        resultMap[key] = value
    }

    for ((key, value) in map) {
        resultMap.merge(key, value) { oldValue, newValue -> oldValue + newValue }
    }

    return resultMap
}
