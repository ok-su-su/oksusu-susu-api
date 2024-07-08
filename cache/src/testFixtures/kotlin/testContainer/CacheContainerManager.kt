package testContainer

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.annotation.AutoScan
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.BeforeProjectListener
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName

@AutoScan
class CacheContainerManager : BeforeProjectListener, AfterProjectListener {
    private val logger = KotlinLogging.logger { }

    companion object {
        private const val REDIS_DOCKER_IMAGE = "redis"
        private const val REDIS_PORT = 6379

        private val redisContainer = GenericContainer(DockerImageName.parse(REDIS_DOCKER_IMAGE))
            .withExposedPorts(REDIS_PORT)
    }

    override suspend fun beforeProject() {
        logger.info { "start redis container" }
        redisContainer.start()
        System.setProperty("spring.redis.host", redisContainer.host)
        System.setProperty("spring.redis.port", redisContainer.getMappedPort(REDIS_PORT).toString())
    }

    override suspend fun afterProject() {
        redisContainer.stop()
        logger.info { "stop redis container" }
    }
}
