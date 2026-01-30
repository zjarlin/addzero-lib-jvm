plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation(libs.gradle.plugin.ksp)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.tool.yml)
    gradleApi()
}
