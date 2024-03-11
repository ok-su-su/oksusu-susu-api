package com.oksusu.susu.api.extension

fun Boolean.toOX(): String {
    if (this) {
        return "O"
    }
    return "X"
}
