package com.goofy.boilerplate.config.database

import com.zaxxer.hikari.HikariDataSource
import org.hibernate.cfg.AvailableSettings
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties
import org.springframework.boot.autoconfigure.orm.jpa.JpaProperties
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Primary
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor
import org.springframework.data.jpa.repository.config.EnableJpaRepositories
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate
import org.springframework.orm.hibernate5.SpringBeanContainer
import org.springframework.orm.jpa.JpaTransactionManager
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean
import org.springframework.transaction.PlatformTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import javax.persistence.EntityManagerFactory
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = ["com.goofy.boilerplate.user.infrastructure"],
    entityManagerFactoryRef = "boilerplateEntityManager",
    transactionManagerRef = "boilerplateTransactionManager"
)
class BoilerPlateDatabaseConfig {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "boilerplate.master.datasource")
    fun boilerplateMasterDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "boilerplate.master.datasource.hikari")
    fun boilerplateMasterHikariDataSource(
        @Qualifier("boilerplateMasterDataSourceProperties") masterProperty: DataSourceProperties
    ): HikariDataSource {
        return masterProperty
            .initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    fun boilerplateNamedParameterJdbcTemplate(
        @Qualifier("boilerplateMasterHikariDataSource") dataSource: DataSource
    ): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "boilerplate.jpa")
    fun boilerplateJpaProperties(): JpaProperties {
        return JpaProperties()
    }

    @Bean
    @Primary
    fun boilerplateEntityManager(
        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
        configurableListableBeanFactory: ConfigurableListableBeanFactory,
        @Qualifier("boilerplateMasterHikariDataSource") boilerplateDataSource: DataSource
    ): LocalContainerEntityManagerFactoryBean {
        return entityManagerFactoryBuilder
            .dataSource(boilerplateDataSource)
            .packages(
                "com.goofy.boilerplate.user.domain"
            )
            .properties(
                mapOf(AvailableSettings.BEAN_CONTAINER to SpringBeanContainer(configurableListableBeanFactory))
            )
            .build()
    }

    @Bean
    @Primary
    fun boilerplateTransactionManager(
        @Qualifier("boilerplateEntityManager") boilerplateEntityManager: EntityManagerFactory
    ): PlatformTransactionManager {
        return JpaTransactionManager(boilerplateEntityManager)
    }

    @Bean
    fun persistenceExceptionTranslationPostProcessor(): PersistenceExceptionTranslationPostProcessor {
        return PersistenceExceptionTranslationPostProcessor()
    }
}
