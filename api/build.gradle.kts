dependencies {
    implementation(project(":domain"))
    implementation(project(":common"))
    implementation(project(":client"))

    /** swagger */
    implementation("org.springdoc:springdoc-openapi-starter-webflux-ui:${DependencyVersion.SPRINGDOC}")
    runtimeOnly("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE}")
    kapt("com.github.therapi:therapi-runtime-javadoc-scribe:${DependencyVersion.JAVADOC_SCRIBE}")

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

    /** jwt */
    implementation("com.auth0:java-jwt:${DependencyVersion.JWT}")
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

springBoot.buildInfo { properties { } }

object DependencyVersion {
    /** jwt */
    const val JWT = "4.4.0"

    /** springdoc */
    const val SPRINGDOC = "2.3.0"
    const val JAVADOC_SCRIBE = "0.15.0"

    /** fastexcel */
    const val FASTEXCEL = "0.17.0"

    /** aws */
    const val AWS_SDK_V2 = "2.24.13"
    const val SPRING_CLOUD_AWS = "3.1.0"

    /** slack */
    const val SLACK_API = "1.38.2"

    /** sentry */
    const val SENTRY = "7.3.0"
}
