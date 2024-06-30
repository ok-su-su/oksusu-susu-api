package com.oksusu.susu.common.extension

fun <T : Any> Map<T, Long>.merge(map: Map<T, Long>): Map<T, Long> {
    val resultMap = mutableMapOf<T, Long>()

    for ((key, value) in this) {
        resultMap[key] = value
    }

    for ((key, value) in map) {
        resultMap.merge(key, value) { oldValue, newValue ->
            oldValue + newValue }
    }

    return resultMap
}

fun <T : Any> Map<T, Pair<Long, Long>>.mergePair(map: Map<T, Pair<Long, Long>>): Map<T, Pair<Long, Long>> {
    val resultMap = mutableMapOf<T, Pair<Long, Long>>()

    for ((key, value) in this) {
        resultMap[key] = value
    }

    for ((key, value) in map) {
        resultMap.merge(
            key,
            value
        ) { oldValue, newValue -> oldValue.first + newValue.first to oldValue.second + newValue.second }
    }

    return resultMap
}

fun <T> Map<String, T>.classifyKeyByPrefix(prefix: String, removePrefix: Boolean = true): Map<String, T> {
    val matchingPrefixMap = mutableMapOf<String, T>()

    this.map { map ->
        if (map.key.startsWith(prefix)) {
            if (removePrefix) {
                val newKey = map.key.removePrefix(prefix)
                matchingPrefixMap[newKey] = map.value
            } else {
                matchingPrefixMap[map.key] = map.value
            }
        }
    }

    return matchingPrefixMap
}
