package site.addzero.gradle.plugin

import org.gradle.accessors.dm.LibrariesForLibs
import site.addzero.gradle.tool.*

plugins {
  `java-library`
}

configureWithSourcesJar()
configUtf8()
configJunitPlatform()

val libs = the<LibrariesForLibs>()
configureJ8(libs.versions.jdk.get())

dependencies {
  testImplementation(libs.junit.jupiter.api)
  testRuntimeOnly(libs.junit.jupiter.engine)
}
