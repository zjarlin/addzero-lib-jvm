import org.gradle.jvm.toolchain.JavaLanguageVersion

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

group = "site.addzero"

dependencies {
    implementation(project(":lib:kcp:multireceiver:kcp-multireceiver-annotations"))
    compileOnly(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)
    testImplementation(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}

val pluginJar = tasks.named<org.gradle.jvm.tasks.Jar>("jar").flatMap { task -> task.archiveFile }

tasks.test {
    dependsOn(pluginJar)
    javaLauncher.set(
        javaToolchains.launcherFor {
            languageVersion.set(JavaLanguageVersion.of(17))
        },
    )
    systemProperty("multireceiver.pluginJar", pluginJar.get().asFile.absolutePath)
}
