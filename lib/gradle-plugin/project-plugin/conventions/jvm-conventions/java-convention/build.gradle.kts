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
    compileOnly(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation(libs.gradle.tool.config.java)
//    testImplementation(libs.junit.jupiter.api)
//    testRuntimeOnly(libs.junit.jupiter.engine)

//    implementation(libs.gradlePlugin.kotlin)

    gradleApi()
}


