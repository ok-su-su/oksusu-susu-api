package com.oksusu.susu.api.testContainer

import io.github.oshai.kotlinlogging.KotlinLogging
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.springframework.beans.factory.InitializingBean
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class DbCleanUp : InitializingBean {
    val logger = KotlinLogging.logger {  }

    @PersistenceContext
    private lateinit var entityManager: EntityManager
    private lateinit var tableNames: List<String>

    @Transactional(readOnly = true)
    override fun afterPropertiesSet() {
        tableNames = entityManager.createNativeQuery("SHOW TABLES").resultList
            .map { it.toString() }
    }

    @Transactional
    fun execute() {
        entityManager.clear()
        entityManager.flush()

        entityManager.createNativeQuery("SET foreign_key_checks = 0").executeUpdate()
        tableNames.forEach { tableName ->
            entityManager.createNativeQuery("TRUNCATE TABLE $tableName").executeUpdate()
        }
        entityManager.createNativeQuery("SET foreign_key_checks = 1").executeUpdate()
    }
}
