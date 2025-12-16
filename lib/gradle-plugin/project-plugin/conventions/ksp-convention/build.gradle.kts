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
    implementation("org.jetbrains.intellij.platform:org.jetbrains.intellij.platform.gradle.plugin:2.10.3")
    implementation("org.jetbrains.changelog:org.jetbrains.changelog.gradle.plugin:2.4.0")
    implementation("org.jetbrains.intellij.platform.migration:org.jetbrains.intellij.platform.migration.gradle.plugin:2.10.3")
    implementation("org.jetbrains.intellij.platform.settings:org.jetbrains.intellij.platform.settings.gradle.plugin:2.10.3")
    implementation("org.jetbrains.intellij.platform.base:org.jetbrains.intellij.platform.base.gradle.plugin:2.10.3")
    implementation("org.jetbrains.intellij.platform.module:org.jetbrains.intellij.platform.module.gradle.plugin:2.10.3")
    gradleApi()
}


