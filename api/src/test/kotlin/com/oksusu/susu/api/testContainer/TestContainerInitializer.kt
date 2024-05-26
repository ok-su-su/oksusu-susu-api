package com.oksusu.susu.api.testContainer

import io.github.oshai.kotlinlogging.KotlinLogging
import org.springframework.boot.test.util.TestPropertyValues
import org.springframework.context.ApplicationContextInitializer
import org.springframework.context.ConfigurableApplicationContext
import org.testcontainers.containers.DockerComposeContainer
import java.io.File

class TestContainerInitializer : ApplicationContextInitializer<ConfigurableApplicationContext> {
    private val logger = KotlinLogging.logger { }

    companion object {
        private const val MYSQL = "mysql"
        private const val MYSQL_PORT = 3306

        private const val REDIS = "redis"
        private const val REDIS_PORT = 6379

        private val dockerCompose = DockerComposeContainer(File("src/test/resources/docker-compose.yml"))
            .withExposedService(MYSQL, MYSQL_PORT)
            .withExposedService(REDIS, REDIS_PORT)
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
        val mysqlProperties = hashMapOf(
            "susu.master.datasource.url" to "jdbc:tc:mysql:8.0.33:///susu",
            "susu.master.datasource.username" to "susu",
            "susu.master.datasource.password" to "susu",
            "susu.master.datasource.hikari.minimum-idle" to 15,
            "susu.master.datasource.hikari.maximum-pool-size" to 25,
            "susu.master.datasource.driver-class-name" to "org.testcontainers.jdbc.ContainerDatabaseDriver"
        )

        properties.plus(mysqlProperties)
    }

    private fun initRedisProperties(properties: Map<String, String>) {
        val redisHost = dockerCompose.getServiceHost(REDIS, REDIS_PORT)
        val redisPort = dockerCompose.getServicePort(REDIS, REDIS_PORT)

        val redisProperties = hashMapOf(
            "spring.data.redis.host" to redisHost,
            "spring.data.redis.port" to redisPort
        )

        properties.plus(redisProperties)
    }
}
