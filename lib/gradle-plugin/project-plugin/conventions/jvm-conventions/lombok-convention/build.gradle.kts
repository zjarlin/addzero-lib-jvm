import org.gradle.accessors.dm.LibrariesForLibs
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

dependencies {
    implementation(libs.gradle.tool.config.java)
    implementation(libs.site.addzero.gradle.plugin.java.convention.gradle.plugin)
    gradleApi()
}


