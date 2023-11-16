package com.goofy.boilerplate.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual

internal class JacocoTest : FunSpec({
    val jacoco = Jacoco()

    context("jacoco test") {
        test("test1") {
            jacoco.jacocoTest() shouldBeEqual "jacocoTest"
        }
    }
})
