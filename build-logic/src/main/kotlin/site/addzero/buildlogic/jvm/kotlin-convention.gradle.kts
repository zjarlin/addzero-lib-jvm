package site.addzero.buildlogic.jvm

import site.addzero.gradle.AdzeroExtension

plugins {
    kotlin("jvm")
    id("site.addzero.buildlogic.jvm.java-convention")
    id("site.addzero.buildlogic.common.configureKotlinCompatibility")
    id("site.addzero.buildlogic.common.ext")

}


val value = the<AdzeroExtension>()

val javaVersion = value.jdkVersion.get().toInt()

kotlin {
    jvmToolchain(javaVersion)
}
