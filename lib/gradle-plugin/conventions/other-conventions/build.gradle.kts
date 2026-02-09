plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.com.diffplug.spotless.gradle.plugin)
    implementation(libs.dokka.gradle.plugin)
    gradleApi()
}
