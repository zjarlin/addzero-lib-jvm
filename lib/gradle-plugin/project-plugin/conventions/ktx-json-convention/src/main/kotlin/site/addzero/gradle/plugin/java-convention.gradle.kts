package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import site.addzero.gradle.tool.configJunitPlatform
import site.addzero.gradle.tool.configUtf8
import site.addzero.gradle.tool.configureJ8
import site.addzero.gradle.tool.configureWithSourcesJar

plugins {
    `java-library`
}

configureWithSourcesJar()
configUtf8()

val libs = the<LibrariesForLibs>()
val javaVersion = libs.versions.jdk.get()
configureJ8(javaVersion)

configJunitPlatform()

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    implementation(libs.org.projectlombok.lombok)
    annotationProcessor(libs.org.projectlombok.lombok)
}

