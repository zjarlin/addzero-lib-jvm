import site.addzero.gradle.tool.configureJ8

buildscript {
    repositories {
        mavenLocal()
        mavenCentral()
        gradlePluginPortal()
    }
    dependencies {
        classpath(libs.site.addzero.gradle.tool.config.java)
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
    implementation(libs.com.google.devtools.ksp.com.google.devtools.ksp.gradle.plugin)
    implementation(libs.kotlin.gradle.plugin)
    implementation(libs.site.addzero.kotlin.convention)
}
