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
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.multiplatform.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.serialization.gradle.plugin)
    implementation(libs.compose.gradle.plugin)
    implementation(libs.org.jetbrains.kotlin.plugin.compose.gradle.plugin)
    implementation(libs.gradle)
    implementation(libs.buildkonfig.gradle.plugin)
    implementation(libs.buildkonfig.compiler)
    implementation(libs.de.jensklingenberg.ktorfit.gradle.plugin)
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    gradleApi()
}
