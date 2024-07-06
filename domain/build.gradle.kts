dependencies {
    implementation(project(":common"))

    /** spring starter */
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    /** querydsl */
    implementation("com.querydsl:querydsl-jpa:${DependencyVersion.QUERYDSL}:jakarta")
    kapt("com.querydsl:querydsl-apt:${DependencyVersion.QUERYDSL}:jakarta")

    /** database */
    runtimeOnly("com.mysql:mysql-connector-j")

    /** test container */
    testFixturesImplementation("org.testcontainers:mysql")

    /** fixture monkey */
    testFixturesImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:${DependencyVersion.FIXTURE_MONKEY}")
}
