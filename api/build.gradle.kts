dependencies {
    implementation(project(":domain"))
    implementation(project(":common"))
    implementation(project(":client"))
    implementation(project(":batch"))
    implementation(project(":cache"))

    testImplementation(testFixtures(project(":cache")))
    testImplementation(testFixtures(project(":domain")))

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
    implementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:${DependencyVersion.SPRING_CLOUD_AWS}"))
    implementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store")

    /** sentry */
    implementation(platform("io.sentry:sentry-bom:${DependencyVersion.SENTRY}"))
    implementation("io.sentry:sentry-spring-boot-starter-jakarta")
    implementation("io.sentry:sentry-logback")

    /** jwt */
    implementation("com.auth0:java-jwt:${DependencyVersion.JWT}")
    implementation("org.bouncycastle:bcpkix-jdk15on:${DependencyVersion.BOUNCY_CASTLE}")
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

springBoot.buildInfo { properties { } }
