dependencies {
    implementation(project(":domain"))
    implementation(project(":common"))
}


tasks.getByName("bootJar") {
    enabled = true
}

tasks.getByName("jar") {
    enabled = false
}
