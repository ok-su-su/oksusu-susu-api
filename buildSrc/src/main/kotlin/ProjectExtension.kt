import org.gradle.api.Project

val Project.isSnapshotVersion: Boolean get() = version.toString().endsWith("SNAPSHOT")
