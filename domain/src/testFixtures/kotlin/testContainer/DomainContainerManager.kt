package testContainer

import io.github.oshai.kotlinlogging.KotlinLogging
import io.kotest.core.annotation.AutoScan
import io.kotest.core.listeners.AfterProjectListener
import io.kotest.core.listeners.BeforeProjectListener
import org.testcontainers.containers.MySQLContainer

@AutoScan
class DomainContainerManager : BeforeProjectListener, AfterProjectListener {
    private val logger = KotlinLogging.logger { }

    companion object {
        private const val MYSQL_DOCKER_IMAGE = "mysql:8.0.33"
        private const val MYSQL_PORT = 3306
        private const val DB_NAME = "susu"

        private val mysqlContainer = MySQLContainer(MYSQL_DOCKER_IMAGE)
            .withExposedPorts(MYSQL_PORT)
            .withDatabaseName(DB_NAME)
            .withInitScript("scripts/DDL.sql")
//            .withInitScript("classpath:scripts/DML.sql")
    }

    override suspend fun beforeProject() {
        logger.info { "start mysql container" }
        mysqlContainer.start()
        System.setProperty("susu.master.datasource.url", mysqlContainer.getJdbcUrl())
        System.setProperty("susu.master.datasource.username", mysqlContainer.username)
        System.setProperty("susu.master.datasource.password", mysqlContainer.password)
        System.setProperty("susu.master.datasource.driver-class-name", mysqlContainer.driverClassName)
    }

    override suspend fun afterProject() {
        mysqlContainer.stop()
        logger.info { "stop mysql container" }
    }
}
