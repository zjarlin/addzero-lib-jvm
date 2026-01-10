import site.addzero.gradle.tool.configureJ8

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.gradle.tool.config.java)
        classpath(libs.com.google.devtools.ksp.gradle.plugin)
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
//     implementation(libs.gradlePlugin.ksp)
    implementation(libs.com.google.devtools.ksp.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
//    implementation(libs.java.convention)
    implementation(libs.kotlin.convention)

//    implementation(libs.kotlin.convention)
    gradleApi()
}


