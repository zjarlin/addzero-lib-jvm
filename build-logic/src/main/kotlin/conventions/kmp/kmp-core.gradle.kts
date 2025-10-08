@file:OptIn(ExperimentalWasmDsl::class)

import gradle.kotlin.dsl.accessors._729aa7c1588b83738f7ec34c0a320432.java
import org.gradle.accessors.dm.LibrariesForLibs
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.text.toInt

val libs = the<LibrariesForLibs>()
plugins {
    id("org.jetbrains.kotlin.multiplatform")
}


val jdkversion = libs.versions.jdk.get()
extensions.configure<JavaPluginExtension> {
    val toVersion = JavaVersion.toVersion(jdkversion)
    sourceCompatibility = toVersion
    targetCompatibility = toVersion
}
java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(jdkversion.toInt()))
    }
}


tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        freeCompilerArgs.set(listOf("-Xjsr305=strict", "-Xjvm-default=all"))
        jvmTarget.set(provider { JvmTarget.fromTarget(java.targetCompatibility.toString()) })
    }
}
kotlin {
    val jdkversion = libs.versions.jdk.get().toInt()
    jvmToolchain(jdkversion)
}


kotlin {
    wasmJs {
        binaries.executable()
        nodejs()
    }
    iosArm64()
    iosSimulatorArm64()
    macosArm64()
//    watchosArm32()
//    watchosArm64()
//    watchosSimulatorArm64()
//    watchosX64()
//    tvosArm64()
//    tvosSimulatorArm64()
//    tvosX64()
//    mingwX64()
//    linuxX64()
//    linuxArm64()
    sourceSets {}
}





