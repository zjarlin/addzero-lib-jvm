plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.com.diffplug.spotless.com.diffplug.spotless.gradle.plugin)
    implementation(libs.org.jetbrains.dokka.dokka.gradle.plugin)
    gradleApi()
}
