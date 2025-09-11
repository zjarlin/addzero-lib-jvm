@file:OptIn(ExperimentalKotlinGradlePluginApi::class)

import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi

plugins {
    id("kmp-ksp")
    id("kmp-json-withtool")
}
// 设置兼容的JDK版本
//java {
//    toolchain {
//        languageVersion.set(JavaLanguageVersion.of(8))
//    }
//}
//
//kotlin {
//    jvmToolchain(8)
//}
kotlin {

    dependencies {
        implementation(libs.pinyin4j)
        implementation(projects.lib.ksp.common.addzeroKspSupport)
        implementation(projects.lib.toolKmp.addzeroToolStr)
    }

}

