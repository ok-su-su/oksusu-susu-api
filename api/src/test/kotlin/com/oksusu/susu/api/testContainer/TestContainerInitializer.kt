package com.oksusu.susu.api.testContainer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.DockerComposeContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.io.File

class TestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val logger = KotlinLogging.logger { }

    companion object {
        private const val MYSQL = "mysql"
        private const val MYSQL_PORT = 3306

        private const val REDIS = "redis"
        private const val REDIS_PORT = 6379

        private val dockerCompose = DockerComposeContainer(File("src/test/resources/docker-compose.yml"))
            .withExposedService(MYSQL, MYSQL_PORT, Wait.forLogMessage(".*ready for connections.*", 1))
            .withExposedService(REDIS, REDIS_PORT, Wait.forLogMessage(".*Ready to accept connections.*", 1))
    }

    override fun initialize(applicationContext: ConfigurableApplicationContext) {
        logger.debug { "start test containers" }

        dockerCompose.start()

        val properties = emptyMap<String, String>()
        initMySQLProperties(properties)
        initRedisProperties(properties)


        TestPropertyValues.of(properties).applyTo(applicationContext)
    }

    private fun initMySQLProperties(properties: Map<String, String>) {
        val rdbmsHost = dockerCompose.getServiceHost(MYSQL, MYSQL_PORT)
        val rdbmsPort = dockerCompose.getServicePort(MYSQL, MYSQL_PORT)

        val mysqlProperties = hashMapOf(
            "susu.master.datasource.url" to "jdbc:mysql://$rdbmsHost:$rdbmsPort/container",
            "susu.master.datasource.username" to "susu",
            "susu.master.datasource.password" to "root",
        )

        properties.plus(mysqlProperties)
    }

    private fun initRedisProperties(properties: Map<String, String>) {
        val redisHost = dockerCompose.getServiceHost(REDIS, REDIS_PORT);
        val redisPort = dockerCompose.getServicePort(REDIS, REDIS_PORT);

        val redisProperties = hashMapOf(
            "spring.data.redis.host" to redisHost,
            "spring.data.redis.port" to redisPort
        )

        properties.plus(redisProperties)
    }
}

