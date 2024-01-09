package com.oksusu.susu.extension

import kotlin.reflect.KProperty1
import kotlin.reflect.full.memberProperties

fun <T> T.getPropertyValues(): Map<String, String> {
    val properties = this!!::class.memberProperties

    return properties.map { property ->
        val convertedProperty = property as KProperty1<Any, *>
        property.name to convertedProperty.get(this).toString()
    }.toMap()
}
