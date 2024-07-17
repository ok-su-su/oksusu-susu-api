package com.oksusu.susu.common.extension

val Throwable.supressedErrorStack: String
    get() = run {
        val exceptionAsStrings = this.suppressedExceptions.flatMap { exception ->
            exception.stackTrace.map { stackTrace ->
                stackTrace.toString()
            }
        }.joinToString(" ")
        val cutLength = exceptionAsStrings.length.coerceAtMost(1000)
        return exceptionAsStrings.substring(0, cutLength)
    }

val Throwable.errorStack: String
    get() = run {
        val exceptionAsStrings = this.stackTrace.joinToString("\n") { stackTrace ->
            stackTrace.toString()
        }
        val cutLength = exceptionAsStrings.length.coerceAtMost(1000)
        return exceptionAsStrings.substring(0, cutLength)
    }
