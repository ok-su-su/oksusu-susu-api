package com.oksusu.susu.common.extension

fun Boolean.toOX(): String {
    if (this) {
        return "O"
    }
    return "X"
}
