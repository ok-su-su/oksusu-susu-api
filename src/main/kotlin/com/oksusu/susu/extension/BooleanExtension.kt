package com.oksusu.susu.extension

fun Boolean.toOX(): String {
    if (this) {
        return "O"
    }
    return "X"
}
