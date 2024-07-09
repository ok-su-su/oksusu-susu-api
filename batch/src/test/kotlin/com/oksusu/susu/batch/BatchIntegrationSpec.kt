package com.oksusu.susu.batch

import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles

@ActiveProfiles("dev")
@SpringBootTest(classes = [BatchIntegrationTestConfiguration::class])
abstract class BatchIntegrationSpec(body: DescribeSpec.() -> Unit = {}) : DescribeSpec(body) {
    override fun extensions(): List<Extension> = listOf(SpringExtension)
}
