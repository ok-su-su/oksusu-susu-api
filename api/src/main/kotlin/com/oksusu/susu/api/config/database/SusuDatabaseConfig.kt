package com.oksusu.susu.api.config.database

import com.zaxxer.hikari.HikariDataSource
import jakarta.persistence.EntityManagerFactory
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
import javax.sql.DataSource

@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = [
        "com.oksusu.susu.api.user.infrastructure",
        "com.oksusu.susu.api.envelope.infrastructure",
        "com.oksusu.susu.api.friend.infrastructure",
        "com.oksusu.susu.api.category.infrastructure",
        "com.oksusu.susu.api.post.infrastructure",
        "com.oksusu.susu.api.term.infrastructure",
        "com.oksusu.susu.api.report.infrastructure",
        "com.oksusu.susu.api.count.infrastructure",
        "com.oksusu.susu.api.log.infrastructure",
        "com.oksusu.susu.api.metadata.infrastructure"
    ],
    entityManagerFactoryRef = "susuEntityManager",
    transactionManagerRef = "susuTransactionManager"
)
class SusuDatabaseConfig {
    @Bean
    @Primary
    @ConfigurationProperties(prefix = "susu.master.datasource")
    fun susuMasterDataSourceProperties(): DataSourceProperties {
        return DataSourceProperties()
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "susu.master.datasource.hikari")
    fun susuMasterHikariDataSource(
        @Qualifier("susuMasterDataSourceProperties") masterProperty: DataSourceProperties,
    ): HikariDataSource {
        return masterProperty
            .initializeDataSourceBuilder()
            .type(HikariDataSource::class.java)
            .build()
    }

    @Bean
    fun susuNamedParameterJdbcTemplate(
        @Qualifier("susuMasterHikariDataSource") dataSource: DataSource,
    ): NamedParameterJdbcTemplate {
        return NamedParameterJdbcTemplate(dataSource)
    }

    @Bean
    @Primary
    @ConfigurationProperties(prefix = "susu.jpa")
    fun susuJpaProperties(): JpaProperties {
        return JpaProperties()
    }

    @Bean
    @Primary
    fun susuEntityManager(
        entityManagerFactoryBuilder: EntityManagerFactoryBuilder,
        configurableListableBeanFactory: ConfigurableListableBeanFactory,
        @Qualifier("susuMasterHikariDataSource") susuDataSource: DataSource,
    ): LocalContainerEntityManagerFactoryBean {
        return entityManagerFactoryBuilder
            .dataSource(susuDataSource)
            .packages(
                "com.oksusu.susu.api.user.domain",
                "com.oksusu.susu.api.envelope.domain",
                "com.oksusu.susu.api.friend.domain",
                "com.oksusu.susu.api.category.domain",
                "com.oksusu.susu.api.post.domain",
                "com.oksusu.susu.api.term.domain",
                "com.oksusu.susu.api.report.domain",
                "com.oksusu.susu.api.count.domain",
                "com.oksusu.susu.api.log.domain",
                "com.oksusu.susu.api.metadata.domain"
            )
            .properties(
                mapOf(AvailableSettings.BEAN_CONTAINER to SpringBeanContainer(configurableListableBeanFactory))
            )
            .build()
    }

    @Bean
    @Primary
    fun susuTransactionManager(
        @Qualifier("susuEntityManager") susuEntityManager: EntityManagerFactory,
    ): PlatformTransactionManager {
        return JpaTransactionManager(susuEntityManager)
    }

    @Bean
    fun persistenceExceptionTranslationPostProcessor(): PersistenceExceptionTranslationPostProcessor {
        return PersistenceExceptionTranslationPostProcessor()
    }
}
