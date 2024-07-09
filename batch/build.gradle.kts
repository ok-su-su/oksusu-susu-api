dependencies {
    implementation(project(":domain"))
    implementation(project(":common"))
    implementation(project(":client"))
    implementation(project(":cache"))

    testImplementation(testFixtures(project(":cache")))
    testImplementation(testFixtures(project(":domain")))

    /** aws v2 */
    testImplementation(platform("software.amazon.awssdk:bom:${DependencyVersion.AWS_SDK_V2}"))
    testImplementation("software.amazon.awssdk:sts")

    /** aws ssm */
    testImplementation(platform("io.awspring.cloud:spring-cloud-aws-dependencies:${DependencyVersion.SPRING_CLOUD_AWS}"))
    testImplementation("io.awspring.cloud:spring-cloud-aws-starter-parameter-store")

    /** fixture monkey */
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-kotlin:${DependencyVersion.FIXTURE_MONKEY}")
    testImplementation("com.navercorp.fixturemonkey:fixture-monkey-starter-kotlin:${DependencyVersion.FIXTURE_MONKEY}")
}
