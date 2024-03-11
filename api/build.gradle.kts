dependencies {
    implementation(project(":domain"))
    implementation(project(":common"))
    implementation(project(":client"))
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

springBoot.buildInfo { properties { } }
