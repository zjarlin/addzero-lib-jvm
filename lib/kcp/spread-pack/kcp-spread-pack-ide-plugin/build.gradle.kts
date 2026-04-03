import org.jetbrains.intellij.platform.gradle.tasks.RunIdeTask

plugins {
    id("site.addzero.buildlogic.intellij.intellij-platform")
}

group = "site.addzero"

dependencies {
    implementation(project(":lib:kcp:spread-pack:kcp-spread-pack-annotations"))
}

tasks.named<RunIdeTask>("runIde") {
    dependsOn(
        ":lib:kcp:spread-pack:kcp-spread-pack-annotations:publishToMavenLocal",
        ":lib:kcp:spread-pack:kcp-spread-pack-plugin:publishToMavenLocal",
        ":lib:kcp:spread-pack:kcp-spread-pack-gradle-plugin:publishToMavenLocal",
    )
    args(
        project.rootDir
            .toPath()
            .resolve("example/example-spread-pack")
            .normalize()
            .toFile()
            .absolutePath,
    )
}
