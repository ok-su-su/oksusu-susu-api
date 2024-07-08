dependencies {
    implementation(project(":common"))

    /** spring starter */
    implementation("org.springframework.boot:spring-boot-starter-data-redis-reactive")

    /** test container */
    testFixturesImplementation("org.testcontainers:testcontainers")
}
