package site.addzero.buildlogic.jvm

import site.addzero.gradle.*

plugins {
    `java-library`
    id("site.addzero.buildlogic.common.ext")
}

val value = the<AdzeroExtension>()
val javaVersion = value.jdkVersion.get()

configureWithSourcesJar()
configUtf8()
configureJavaCompatibility(javaVersion)
configJavaToolChain(javaVersion)
configJunitPlatform()
