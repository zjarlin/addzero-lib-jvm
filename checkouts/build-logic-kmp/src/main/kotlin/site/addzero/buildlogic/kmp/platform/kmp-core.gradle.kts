@file:OptIn(ExperimentalWasmDsl::class)

package site.addzero.buildlogic.kmp.platform

import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.targets.jvm.KotlinJvmTarget

plugins {
  id("org.jetbrains.kotlin.multiplatform")
}
kotlin {
    jvmToolchain(8)

    targets.withType<KotlinJvmTarget>().configureEach {
        compilations.configureEach {
            compileTaskProvider.configure {
                compilerOptions {
                    jvmTarget.set(JvmTarget.JVM_1_8)
                }
            }
        }
    }

    mingwX64()
//    linuxX64()
    jvm()

    wasmJs {
      browser()
    }
//    iosX64()
//    macosArm64()
    //todo 暂时不开发ios应用
//    iosArm64()


    //模拟器
//    iosSimulatorArm64()
//    macosX64()
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

}
