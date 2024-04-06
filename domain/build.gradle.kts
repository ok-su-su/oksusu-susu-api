dependencies {
    implementation(project(":common"))
    implementation(project(":cache"))

    /** spring starter */
    api("org.springframework.boot:spring-boot-starter-data-jpa")

    /** querydsl */
    implementation("com.querydsl:querydsl-jpa:${DependencyVersion.QUERYDSL}:jakarta")
    kapt("com.querydsl:querydsl-apt:${DependencyVersion.QUERYDSL}:jakarta")

    /** database */
    runtimeOnly("com.mysql:mysql-connector-j")
}
