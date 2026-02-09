import site.addzero.gradle.tool.configureJ8

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
    }
    dependencies {
        classpath(libs.gradle.tool.config.java)
    }
}
configureJ8("17")

plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

version="2026.02.04"
dependencies {
    /////////////////intellij///////////////
    implementation(libs.org.jetbrains.intellij.platform.gradle.plugin)
    implementation(libs.org.jetbrains.changelog.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.migration.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.settings.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.base.gradle.plugin)
    implementation(libs.org.jetbrains.intellij.platform.module.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.kotlin.convention)
    gradleApi()
}


