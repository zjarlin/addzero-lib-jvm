package site.addzero.buildlogic.jvm

import org.gradle.accessors.dm.LibrariesForLibs
import site.addzero.gradle.tool.configJunitPlatform
import site.addzero.gradle.tool.configUtf8
import site.addzero.gradle.tool.configureJ8
import site.addzero.gradle.tool.configureWithSourcesJar

plugins {
    `java-library`
}

val libs = the<LibrariesForLibs>()
val javaVersion = libs.versions.jdk.get()
configureWithSourcesJar()
configUtf8()

configureJ8("8")
//configureJavaCompatibility(javaVersion)
//configJavaToolChain(javaVersion)
configJunitPlatform()

dependencies {
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
}
