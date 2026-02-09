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
configureJ8("8")

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
    api(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.gradle.tool.config.java)
    implementation(libs.kotlin.gradle.plugin)
//  implementation("java-convention")
    api(libs.site.addzero.gradle.plugin.java.convention.gradle.plugin)
}

