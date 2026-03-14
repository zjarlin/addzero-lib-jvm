import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("site.addzero.buildlogic.intellij.intellij-platform")
}

dependencies {
    implementation(project(":lib:kcp:transform-overload:kcp-transform-overload-annotations"))
    testImplementation(libs.junit.junit)
}

tasks.test {
  useJUnit()
}

tasks.named<RunIdeTask>("runIde") {
    dependsOn(
        ":lib:kcp:transform-overload:kcp-transform-overload-annotations:publishToMavenLocal",
        ":lib:kcp:transform-overload:kcp-transform-overload-plugin:publishToMavenLocal",
        ":lib:kcp:transform-overload:kcp-transform-overload-gradle-plugin:publishToMavenLocal",
    )
    args(
        project.rootDir
            .toPath()
            .resolve("example-transform-overload")
            .normalize()
            .toFile()
            .absolutePath,
    )
}
