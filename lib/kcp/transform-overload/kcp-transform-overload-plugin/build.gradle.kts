import org.gradle.jvm.tasks.Jar

plugins {
    id("site.addzero.buildlogic.jvm.kotlin-convention")
}

group = "site.addzero"

dependencies {
    implementation(project(":lib:kcp:transform-overload:kcp-transform-overload-annotations"))
    compileOnly(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)
    testImplementation(libs.org.jetbrains.kotlin.kotlin.compiler.embeddable)
    testImplementation(libs.org.junit.jupiter.junit.jupiter)
}

val pluginJar = tasks.named<Jar>("jar").flatMap { task -> task.archiveFile }

tasks.test {
    dependsOn(pluginJar)
    systemProperty("transformOverload.pluginJar", pluginJar.get().asFile.absolutePath)
}
