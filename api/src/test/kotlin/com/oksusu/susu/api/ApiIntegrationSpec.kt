package com.oksusu.susu.api

import com.oksusu.susu.api.testContainer.TestContainerInitializer
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import org.springframework.test.context.event.RecordApplicationEvents

@ActiveProfiles("dev", "test")
@SpringBootTest(classes = [ApiIntegrationTestConfiguration::class])
@ContextConfiguration(initializers = [TestContainerInitializer::class])
abstract class ApiIntegrationSpec(body: DescribeSpec.() -> Unit = {}) : DescribeSpec(body) {
    override fun extensions(): List<Extension> = listOf(SpringExtension)
}
