package com.oksusu.susu.extension

import com.fasterxml.jackson.core.type.TypeReference
import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

fun <T : Any> T.getPropertyValues(): Map<String, String> {
    val properties = this::class.memberProperties

    return properties.associate { property ->
        val convertedProperty = property as KProperty1<Any, *>

        property.name to convertedProperty.get(this).toString()
    }
}

fun <T> Class<T>.toTypeReference(): TypeReference<T> {
    return object : TypeReference<T>() {}
}
