import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

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
        if (hasTask(":build") || hasTask(":api:build")) {
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
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

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
        kapt("org.springframework.boot:spring-boot-configuration-processor")

        /** kotlin */
        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

        /** arrow-kt */
        implementation("io.arrow-kt:arrow-fx-coroutines:${DependencyVersion.ARROW_FX}")
        implementation("io.arrow-kt:arrow-fx-stm:${DependencyVersion.ARROW_FX}")

        /** logger */
        implementation("io.github.oshai:kotlin-logging-jvm:${DependencyVersion.KOTLIN_LOGGING}")
        implementation("net.logstash.logback:logstash-logback-encoder:${DependencyVersion.LOGBACK_ENCODER}")

        /** etc */
        developmentOnly("org.springframework.boot:spring-boot-devtools")

        /** test **/
        testImplementation("org.springframework.boot:spring-boot-starter-test")
        testImplementation("io.mockk:mockk:${DependencyVersion.MOCKK}")

        /** kotest */
        testImplementation("io.kotest:kotest-runner-junit5:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest:kotest-assertions-core:${DependencyVersion.KOTEST}")
        testImplementation("io.kotest.extensions:kotest-extensions-spring:${DependencyVersion.KOTEST_EXTENSION}")
        testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:${DependencyVersion.FIXTURE_MONKEY}")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:${DependencyVersion.COROUTINE_TEST}")
    }

    tasks.getByName("bootJar") {
        enabled = false
    }

    tasks.getByName("jar") {
        enabled = true
    }

    configurations.all {
        resolutionStrategy.cacheChangingModulesFor(0, "seconds")
    }

    defaultTasks("bootRun")

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
