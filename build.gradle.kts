import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.8.22"

    id("org.springframework.boot") version "2.7.17"
    id("io.spring.dependency-management") version "1.0.15.RELEASE"
    kotlin("jvm") version kotlinVersion
    kotlin("plugin.spring") version kotlinVersion
    kotlin("plugin.jpa") version kotlinVersion
    kotlin("plugin.allopen") version kotlinVersion
    kotlin("kapt") version kotlinVersion
    idea

    /** ktlint **/
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1"

    /** jacoco **/
    id("jacoco")
}

group = "com.goofy"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    mavenCentral()
}

idea {
    module {
        val kaptMain = file("build/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
    }
}

/**
 * https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
 * automatically supported annotation
 * @Component, @Async, @Transactional, @Cacheable, @SpringBootTest,
 * @Configuration, @Controller, @RestController, @Service, @Repository.
 * jpa meta-annotations not automatically opened through the default settings of the plugin.spring
 */
allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

springBoot.buildInfo { properties { } }

object DependencyVersion {
    /** external */
    const val QUERYDSL_VERSION = "5.0.0"
    const val ARROW_FX_VERSION = "1.1.3"
    const val SPRINGDOC_VERSION = "1.6.15"
    const val JAVADOC_SCRIBE_VERSION = "0.15.0"
    const val KOTLIN_LOGGING_VERSION = "2.0.11"
    const val LOGBACK_ENCODER_VERSION = "6.6"
}

dependencies {
    /** kotlin */
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    /** spring starter */
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    kapt("org.springframework.boot:spring-boot-configuration-processor")

    /** querydsl */
    implementation("com.querydsl:querydsl-collections:${DependencyVersion.QUERYDSL_VERSION}")
    implementation("com.querydsl:querydsl-sql:${DependencyVersion.QUERYDSL_VERSION}")
    implementation("com.querydsl:querydsl-jpa:${DependencyVersion.QUERYDSL_VERSION}")
    kapt("com.querydsl:querydsl-apt:${DependencyVersion.QUERYDSL_VERSION}:jpa")

    /** arrow-kt */
    implementation("io.arrow-kt:arrow-fx-coroutines:${DependencyVersion.ARROW_FX_VERSION}")
    implementation("io.arrow-kt:arrow-fx-stm:${DependencyVersion.ARROW_FX_VERSION}")

    /** swagger */
    implementation("org.springdoc:springdoc-openapi-webflux-ui:${DependencyVersion.SPRINGDOC_VERSION}")
    implementation("org.springdoc:springdoc-openapi-kotlin:${DependencyVersion.SPRINGDOC_VERSION}")
    implementation("org.springdoc:springdoc-openapi-javadoc:${DependencyVersion.SPRINGDOC_VERSION}")
    kapt("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE_VERSION}")

    /** database */
    runtimeOnly("com.mysql:mysql-connector-j")

    /** logger */
    implementation("io.github.microutils:kotlin-logging:${DependencyVersion.KOTLIN_LOGGING_VERSION}")
    implementation("net.logstash.logback:logstash-logback-encoder:${DependencyVersion.LOGBACK_ENCODER_VERSION}")

    /** test **/
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.kotest:kotest-runner-junit5:5.7.2")
    testImplementation("io.kotest:kotest-assertions-core:5.7.2")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:1.1.2")

    /** etc */
    developmentOnly("org.springframework.boot:spring-boot-devtools")
}

defaultTasks("bootRun")

configurations.all {
    resolutionStrategy.cacheChangingModulesFor(0, "seconds")
}

tasks.getByName<Jar>("jar") {
    enabled = false
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "11"
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "8.4"
}

when {
    project.hasProperty("prod") -> {
        println("Profile: prod")
        apply {
            from("profile_prod.gradle")
        }
    }

    project.hasProperty("staging") -> {
        println("Profile: staging")
        apply {
            from("profile_staging.gradle")
        }
    }

    else -> {
        println("Profile: dev")
        apply {
            from("profile_dev.gradle")
        }
    }
}

val Project.isSnapshotVersion: Boolean get() = version.toString().endsWith("SNAPSHOT")

tasks.test {
    finalizedBy(tasks.jacocoTestReport) // 테스트 후 진행
}

tasks.jacocoTestReport {
    dependsOn(tasks.test) // 테스트 선행 필요
    reports {
        html.required.set(true)
        csv.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(File("build/reports/jacoco.xml"))
    }

    classDirectories.setFrom(
            files(classDirectories.files.map {
                fileTree(it) {
                    exclude("**/*Application*",
                            "**/*Config*",
                            "**/*Dto*",
                            "**/*Request*",
                            "**/*Response*",
                            "**/*Interceptor*",
                            "**/*Exception*" ,
                            "**/Q*.class") // Query Dsl 용
                }
            })
    )
}