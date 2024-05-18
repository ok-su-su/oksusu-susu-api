package com.oksusu.susu.api

import com.oksusu.susu.api.testContainer.DbCleanUp
import com.oksusu.susu.api.testContainer.TestContainerInitializer
import com.oksusu.susu.common.extension.withMDCContext
import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.extensions.Extension
import io.kotest.core.spec.Spec
import io.kotest.core.spec.style.DescribeSpec
import io.kotest.extensions.spring.SpringExtension
import kotlinx.coroutines.Dispatchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import java.util.concurrent.atomic.AtomicInteger

@ActiveProfiles("dev")
@SpringBootTest(classes = [ApiIntegrationTestConfiguration::class])
@ContextConfiguration(initializers = [TestContainerInitializer::class])
abstract class IntegrationSpec(body: DescribeSpec.() -> Unit = {}) : DescribeSpec(body) {
    val logger = KotlinLogging.logger { }
    private val counter = AtomicInteger(0)

    @Autowired
    private lateinit var dbCleanUp: DbCleanUp

    override fun extensions(): List<Extension> = listOf(SpringExtension)

    override suspend fun beforeSpec(spec: Spec) {
        withMDCContext(Dispatchers.IO) {
            dbCleanUp.execute()
        }

//        if (counter.getAndAdd(1) == 0) {
//            try {
//                dynamoDBSetUp.createTable()
//            } catch (e: ResourceInUseException) {
//                logger.info("dynamo db setup exception ${e.message}")
//            }
//        }
    }
}
