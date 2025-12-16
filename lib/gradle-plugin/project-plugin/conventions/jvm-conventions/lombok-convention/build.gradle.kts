import org.gradle.accessors.dm.LibrariesForLibs
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
    implementation("site.addzero:gradle-tool-config-java:0.0.674")
    implementation("site.addzero.gradle.plugin.java-convention:site.addzero.gradle.plugin.java-convention.gradle.plugin:2025.12.19")
    gradleApi()
}


