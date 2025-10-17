package site.addzero.buildlogic.common

import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.jvm.toolchain.JavaLanguageVersion
import org.gradle.kotlin.dsl.configure

plugins {
    id("site.addzero.buildlogic.common.ext")
}

val value = the<site.addzero.gradle.AdzeroExtension>()

val javaVersion = value.jdkVersion.get().toInt()

extensions.configure<JavaPluginExtension> {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(javaVersion))
    }
}
