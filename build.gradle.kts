import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    val kotlinVersion = "1.8.22"

    id("org.springframework.boot") version "3.1.5"
    id("io.spring.dependency-management") version "1.1.3"
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

    /** sonarqube **/
    id("org.sonarqube") version "4.3.1.3277"
}

group = "com.oksusu"
java.sourceCompatibility = JavaVersion.VERSION_17

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
    const val SPRINGDOC_VERSION = "2.2.0"
    const val JAVADOC_SCRIBE_VERSION = "0.15.0"
    const val KOTLIN_LOGGING_VERSION = "2.0.11"
    const val LOGBACK_ENCODER_VERSION = "6.6"
    const val KOTEST_VERSION = "5.7.2"
    const val KOTEST_EXTENSION_VERSION = "1.1.2"
    const val MOCKK_VERSION = "1.4.1"
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
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${DependencyVersion.SPRINGDOC_VERSION}")
    kapt("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE_VERSION}")

    /** database */
    runtimeOnly("com.mysql:mysql-connector-j")

    /** logger */
    implementation("io.github.microutils:kotlin-logging:${DependencyVersion.KOTLIN_LOGGING_VERSION}")
    implementation("net.logstash.logback:logstash-logback-encoder:${DependencyVersion.LOGBACK_ENCODER_VERSION}")

    /** test **/
    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("io.mockk:mockk:${DependencyVersion.MOCKK_VERSION}")

    /** kotest */
    testImplementation("io.kotest:kotest-runner-junit5:${DependencyVersion.KOTEST_VERSION}")
    testImplementation("io.kotest:kotest-assertions-core:${DependencyVersion.KOTEST_VERSION}")
    testImplementation("io.kotest.extensions:kotest-extensions-spring:${DependencyVersion.KOTEST_EXTENSION_VERSION}")

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
        jvmTarget = "17"
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

/** jacoco **/
tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.test {
    /** when finished test-all */
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        html.required.set(true)
        csv.required.set(false)
        xml.required.set(true)
        xml.outputLocation.set(File("$buildDir/reports/jacoco.xml"))
    }

    classDirectories.setFrom(
        files(
            classDirectories.files.map {
                fileTree(it) {
                    exclude(
                        "**/*Application*",
                        "**/*Config*",
                        "**/*Dto*",
                        "**/*Request*",
                        "**/*Response*",
                        "**/*Interceptor*",
                        "**/*Exception*",
                        "**/Q*.class"
                    )
                }
            }
        )
    )
}

/** sonarqube **/
sonarqube {
    properties {
        property("sonar.projectKey", "YAPP-Github_23rd-Android-Team-1-BE")
        property("sonar.organization", "yapp-github")
        property("sonar.host.url", "https://sonarcloud.io")

        /** sonar additional settings */
        property("sonar.sources", "src")
        property("sonar.language", "Kotlin")
        property("sonar.sourceEncoding", "UTF-8")
        property("sonar.test.inclusions", "**/*Test.java")
        property(
            "sonar.exclusions",
            "**/test/**, **/Q*.kt, **/*Doc*.kt, **/resources/** ,**/*Application*.kt , **/*Config*.kt, " +
                "**/*Dto*.kt, **/*Request*.kt, **/*Response*.kt ,**/*Exception*.kt ,**/*ErrorCode*.kt"
        )
        property("sonar.java.coveragePlugin", "jacoco")
        property("sonar.java.binaries", "$buildDir/classes")
        property("sonar.coverage.jacoco.xmlReportPaths", "$buildDir/reports/jacoco.xml")
    }
}
