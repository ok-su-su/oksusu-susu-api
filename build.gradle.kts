import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

object DependencyVersion {
    /** querydsl */
    const val QUERYDSL = "5.0.0"

    /** arrow fx */
    const val ARROW_FX = "1.2.1"

    /** jwt */
    const val JWT = "4.4.0"

    /** springdoc */
    const val SPRINGDOC = "2.3.0"
    const val JAVADOC_SCRIBE = "0.15.0"

    /** log */
    const val KOTLIN_LOGGING = "6.0.3"
    const val LOGBACK_ENCODER = "7.4"

    /** fastexcel */
    const val FASTEXCEL = "0.17.0"

    /** aws */
    const val AWS_SDK_V2 = "2.24.13"
    const val SPRING_CLOUD_AWS = "3.1.0"

    /** slack */
    const val SLACK_API = "1.38.2"

    /** sentry */
    const val SENTRY = "7.3.0"

    /** test */
    const val MOCKK = "1.13.9"
    const val KOTEST = "5.8.0"
    const val KOTEST_EXTENSION = "1.1.3"
}

plugins {
    val kotlinVersion = "1.9.22"

    id("org.springframework.boot") version "3.2.3"
    id("io.spring.dependency-management") version "1.1.4"
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

java.sourceCompatibility = JavaVersion.VERSION_17

idea {
    module {
        val kaptMain = file("build/generated/source/kapt/main")
        sourceDirs.add(kaptMain)
        generatedSourceDirs.add(kaptMain)
    }
}


allprojects {
    group = "com.oksusu"

    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "idea")

    repositories {
        mavenCentral()
    }

    tasks.withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "17"
        }
    }

    tasks.withType<Wrapper> {
        gradleVersion = "8.5"
    }

    tasks.withType<Test> {
        useJUnitPlatform()
    }

    /** build시 ktlint 미적용 */
    gradle.taskGraph.whenReady {
        if (hasTask(":build")) {
            allTasks.forEach { task ->
                if (task.name.contains("ktlint") || task.name.contains("Ktlint")) {
                    task.enabled = false
                }
            }
        }
    }
}

tasks.getByName("bootJar") {
    enabled = false
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.jpa")
    apply(plugin = "org.jetbrains.kotlin.plugin.allopen")
    apply(plugin = "org.jetbrains.kotlin.kapt")
    apply(plugin = "jacoco")

    /**
     * https://kotlinlang.org/docs/reference/compiler-plugins.html#spring-support
     * automatically supported annotation
     * @Component, @Async, @Transactional, @Cacheable, @SpringBootTest,
     * @Configuration, @Controller, @RestController, @Service, @Repository.
     * jpa meta-annotations not automatically opened through the default settings of the plugin.spring
     */
    allOpen {
        annotation("jakarta.persistence.Entity")
        annotation("jakarta.persistence.MappedSuperclass")
        annotation("jakarta.persistence.Embeddable")
    }

    dependencies {
        /** spring starter */
        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-actuator")
        implementation("org.springframework.boot:spring-boot-starter-validation")
        implementation("org.springframework.boot:spring-boot-starter-data-jpa")
        implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")
        kapt("org.springframework.boot:spring-boot-configuration-processor")

        /** kotlin */
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        /** querydsl */
        implementation("com.querydsl:querydsl-jpa:${DependencyVersion.QUERYDSL}:jakarta")
        kapt("com.querydsl:querydsl-apt:${DependencyVersion.QUERYDSL}:jakarta")

        /** arrow-kt */
        implementation("io.arrow-kt:arrow-fx-coroutines:${DependencyVersion.ARROW_FX}")
        implementation("io.arrow-kt:arrow-fx-stm:${DependencyVersion.ARROW_FX}")

        /** jwt */
        implementation("com.auth0:java-jwt:${DependencyVersion.JWT}")

        /** swagger */
        implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${DependencyVersion.SPRINGDOC}")
        runtimeOnly("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE}")
        kapt("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE}")

        /** database */
        runtimeOnly("com.mysql:mysql-connector-j")

        /** logger */
        implementation("io.github.oshai:kotlin-logging-jvm:${DependencyVersion.KOTLIN_LOGGING}")
        implementation("net.logstash.logback:logstash-logback-encoder:${DependencyVersion.LOGBACK_ENCODER}")

        /** thymeleaf */
        implementation("org.springframework.boot:spring-boot-starter-thymeleaf")

        /** fastexcel */
        implementation("org.dhatim:fastexcel:${DependencyVersion.FASTEXCEL}")

        /** aws v2 */
        implementation(platform("software.amazon.awssdk:bom:${DependencyVersion.AWS_SDK_V2}"))
        implementation("software.amazon.awssdk:sts")

        /** aws ssm */
        implementation(
            platform("io.awspring.cloud:spring-cloud-aws-dependencies:${DependencyVersion.SPRING_CLOUD_AWS}")
        )
        implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store")

        /** slack */
        implementation("com.slack.api:slack-api-client:${DependencyVersion.SLACK_API}")

        /** sentry */
        implementation(platform("io.sentry:sentry-bom:${DependencyVersion.SENTRY}"))
        implementation("io.sentry:sentry-spring-boot-starter-jakarta")
        implementation("io.sentry:sentry-logback")

        /** etc */
        developmentOnly("org.springframework.boot:spring-boot-devtools")

        /** test **/
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.mockk:mockk:${DependencyVersion.MOCKK}")

        /** kotest */
        testImplementation("io.kotest:kotest-runner-junit5:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest:kotest-assertions-core:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:${DependencyVersion.KOTEST_EXTENSION}")
    }

    tasks.getByName("jar") {
        enabled = true
    }

    tasks.getByName("bootJar") {
        enabled = false
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }

    defaultTasks("bootRun")
}


when {
    project.hasProperty("prod") -> {
        println("Profile: prod")
        apply {
            from(rootProject.file("/profile_prod.gradle").path)
        }
    }

    project.hasProperty("staging") -> {
        println("Profile: staging")
        apply {
            from(rootProject.file("profile_staging.gradle").path)
        }
    }

    else -> {
        println("Profile: dev")
        apply {
            from(rootProject.file("profile_dev.gradle").path)
        }
    }
}

val Project.isSnapshotVersion: Boolean get() = version.toString().endsWith("SNAPSHOT")

/** jacoco **/
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
        xml.outputLocation.set(project.layout.buildDirectory.dir("reports/jacoco.xml").get().asFile)
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
        property("sonar.java.binaries", project.layout.buildDirectory.dir("/classes").get().asFile.path)
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            project.layout.buildDirectory.dir("/reports/jacoco.xml").get().asFile.path
        )
    }
}
