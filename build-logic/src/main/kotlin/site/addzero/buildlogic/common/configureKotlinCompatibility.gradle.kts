package site.addzero.buildlogic.common

import org.gradle.kotlin.dsl.`java-library`
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("site.addzero.buildlogic.common.ext")
    `java-library`
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
        jvmTarget.set(provider { JvmTarget.fromTarget(java.targetCompatibility.toString()) })
    }
}
