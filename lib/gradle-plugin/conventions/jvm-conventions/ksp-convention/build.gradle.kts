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
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
//     implementation(libs.gradlePlugin.ksp)
    implementation(libs.gradle.plugin.ksp)
    implementation(libs.gradlePlugin.kotlin)
    implementation(libs.kotlin.convention)
}
