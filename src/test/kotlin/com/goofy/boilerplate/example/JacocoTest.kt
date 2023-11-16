package com.goofy.boilerplate.example

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class JacocoTest @Autowired constructor(
    private val jacoco: Jacoco,
) : FunSpec({
    context("jacoco test") {
        test("test1") {
            jacoco.jacocoTest() shouldBeEqual "jacocoTest"
        }
    }
})
