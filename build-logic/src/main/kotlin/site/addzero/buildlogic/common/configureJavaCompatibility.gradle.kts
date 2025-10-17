package site.addzero.buildlogic.common

import org.gradle.api.JavaVersion
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure

plugins {
    id("site.addzero.buildlogic.common.ext")
}
val value = the<site.addzero.gradle.AdzeroExtension>()
val javaVersion = value.jdkVersion.get().toInt()
extensions.configure<JavaPluginExtension> {
    sourceCompatibility = JavaVersion.toVersion(javaVersion)
    targetCompatibility = JavaVersion.toVersion(javaVersion)
}
