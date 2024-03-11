dependencies {
    implementation(project(":domain"))
}

tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}

springBoot.buildInfo { properties { } }
