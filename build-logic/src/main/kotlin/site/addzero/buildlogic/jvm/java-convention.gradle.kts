package site.addzero.buildlogic.jvm

import site.addzero.gradle.tool.configJunitPlatform
import site.addzero.gradle.tool.configUtf8
import site.addzero.gradle.tool.configureJdk
import site.addzero.gradle.tool.configureWithSourcesJar

plugins {
    `java-library`
}

val libs = versionCatalogs.named("libs")
val javaVersion = libs.findVersion("jdk").get().requiredVersion
val jdk8Version = libs.findVersion("jdk8").get().requiredVersion
configureWithSourcesJar()
configUtf8()

configureJdk(jdk8Version)
configJunitPlatform()

dependencies {
    testImplementation(libs.findLibrary("org-junit-jupiter-junit-jupiter-api").get())
    testRuntimeOnly(libs.findLibrary("org-junit-jupiter-junit-jupiter-engine").get())
}
