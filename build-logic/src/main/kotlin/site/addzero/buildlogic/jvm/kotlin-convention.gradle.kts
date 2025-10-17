package site.addzero.buildlogic.jvm

import site.addzero.gradle.AdzeroExtension
import site.addzero.gradle.configureKotlinCompatibility
import site.addzero.gradle.configureKotlinToolchain

plugins {
    kotlin("jvm")
    id("site.addzero.buildlogic.jvm.java-convention")
    id("site.addzero.buildlogic.common.ext")

}
val value = the<AdzeroExtension>()
val javaVersion = value.jdkVersion.get()
configureKotlinCompatibility()
configureKotlinToolchain(javaVersion)
