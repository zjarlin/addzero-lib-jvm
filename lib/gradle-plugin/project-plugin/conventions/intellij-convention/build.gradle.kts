import site.addzero.gradle.tool.configureJ8

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath("site.addzero:gradle-tool-config-java:0.0.674")
    }
}
configureJ8("8")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    implementation("site.addzero:kotlin-convention:2025.12.20")
    /////////////////intellij///////////////
    implementation(libs.org.jetbrains.intellij.platform.gradle.plugin)
    implementation(libs.org.jetbrains.changelog.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.migration.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.settings.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.base.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.module.gradle.plugin)
    gradleApi()
}


