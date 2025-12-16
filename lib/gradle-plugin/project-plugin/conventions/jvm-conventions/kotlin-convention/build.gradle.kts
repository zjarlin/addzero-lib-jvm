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
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.gradle.tool.config.java)
    implementation(libs.gradlePlugin.kotlin)
    api("site.addzero.gradle.plugin.java-convention:site.addzero.gradle.plugin.java-convention.gradle.plugin:2025.12.19")
//    testImplementation(libs.junit.jupiter.api)
//    testRuntimeOnly(libs.junit.jupiter.engine)

//    implementation(libs.gradlePlugin.kotlin)

    gradleApi()
}


