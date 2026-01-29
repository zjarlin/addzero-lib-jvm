import site.addzero.gradle.tool.configureJ8

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.gradle.tool.config.java)
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
    implementation(libs.gradle.plugin.ksp)
    implementation(libs.gradlePlugin.kotlin)
//    implementation(libs.java.convention)
    implementation(libs.kotlin.convention)

//    implementation(libs.kotlin.convention)
    gradleApi()
}


