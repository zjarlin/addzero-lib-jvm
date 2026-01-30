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

configureJ8(libs.versions.jdkHigh.get())
plugins {
    `kotlin-dsl`
    `java-gradle-plugin`
}


repositories {
    mavenCentral()
    gradlePluginPortal()
    google()
}

dependencies {
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.kotlin.convention)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.org.jetbrains.kotlin.multiplatform.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.gradlePlugin.jetbrainsCompose)
    implementation(libs.gradlePlugin.composeCompiler)
    implementation(libs.gradlePlugin.android)
    implementation(libs.gradlePlugin.buildkonfig)
    implementation(libs.gradlePlugin.buildkonfig.cp)
    implementation(libs.gradlePlugin.ktorfit)
    implementation(libs.gradle.plugin.ksp)
    gradleApi()
}
